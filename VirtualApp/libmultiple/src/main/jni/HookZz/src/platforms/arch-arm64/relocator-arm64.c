#include "relocator-arm64.h"
#include <assert.h>
#include <stdlib.h>
#include <string.h>

#define MAX_RELOCATOR_INSTRUCIONS_SIZE 64

void arm64_relocator_init(ARM64Relocator *relocator, ARM64Reader *input, ARM64AssemblerWriter *output) {
    memset(relocator, 0, sizeof(ARM64Relocator));
    relocator->inpos                = 0;
    relocator->outpos               = 0;
    relocator->input                = input;
    relocator->output               = output;
    relocator->try_relocated_length = 0;

    //    relocator->literal_insns =
    //        (ARM64Instruction **)malloc0(MAX_LITERAL_INSN_SIZE * sizeof(ARM64Instruction *));
}

void arm64_relocator_free(ARM64Relocator *relocator) {

    arm64_reader_free(relocator->input);
    arm64_writer_free(relocator->output);
    free(relocator);
}

void arm64_relocator_reset(ARM64Relocator *self, ARM64Reader *input, ARM64AssemblerWriter *output) {
    self->inpos                = 0;
    self->outpos               = 0;
    self->input                = input;
    self->output               = output;
    self->literal_insn_size    = 0;
    self->try_relocated_length = 0;
}

void arm64_relocator_read_one(ARM64Relocator *self, ARM64Instruction *instruction) {
    ARM64Instruction *insn_ctx;

    arm64_reader_read_one_instruction(self->input);

    // switch (1) {}

    self->inpos++;

    if (instruction != NULL)
        *instruction = *insn_ctx;
}

void arm64_relocator_try_relocate(zz_ptr_t address, zz_size_t min_bytes, zz_size_t *max_bytes) {
    int tmp_size          = 0;
    bool early_end        = FALSE;
    zz_addr_t target_addr = (zz_addr_t)address;
    ARM64Instruction *insn_ctx;
    ARM64Reader *reader = arm64_reader_new(address);

    do {
        insn_ctx = arm64_reader_read_one_instruction(reader);
        switch (GetARM64InsnType(insn_ctx->insn)) {
        case ARM64_INS_B:
            early_end = TRUE;
            break;
        default:;
        }
        tmp_size += insn_ctx->size;
        target_addr = target_addr + insn_ctx->size;
    } while (tmp_size < min_bytes);

    if (early_end) {
        *max_bytes = tmp_size;
    }

    arm64_reader_free(reader);
    return;
}

static ARM64RelocatorInstruction *arm64_relocator_get_relocator_insn_with_address(ARM64Relocator *self,
                                                                                       zz_addr_t insn_address) {
    for (int i = 0; i < self->relocator_insn_size; ++i) {
        if ((self->relocator_insns[i].origin_insn->pc) == insn_address) {
            return &self->relocator_insns[i];
        }
    }
    return NULL;
}

void arm64_relocator_relocate_writer(ARM64Relocator *relocator, zz_addr_t final_relocate_address) {
    ARM64RelocatorInstruction *relocated_insn;
    if (relocator->literal_insn_size) {
        zz_addr_t *literal_target_address_ptr;
        for (int i = 0; i < relocator->literal_insn_size; i++) {
            literal_target_address_ptr = (zz_addr_t *)relocator->literal_insns[i]->address;
            // literal instruction in the range of instructions-need-fix
            if (*literal_target_address_ptr > relocator->input->start_pc &&
                *literal_target_address_ptr < (relocator->input->start_pc + relocator->input->size)) {
                relocated_insn =
                    arm64_relocator_get_relocator_insn_with_address(relocator, *literal_target_address_ptr);
                assert(relocated_insn);
                *literal_target_address_ptr =
                    (*relocated_insn->relocated_insns)->pc - relocator->output->start_pc + final_relocate_address;
            }
        }
    }
}

void arm64_relocator_write_all(ARM64Relocator *self) {
    int count                           = 0;
    int outpos                          = self->outpos;
    ARM64AssemblerWriter arm64_writer = *self->output;

    while (arm64_relocator_write_one(self))
        count++;
}

static void arm64_relocator_register_literal_insn(ARM64Relocator *self, ARM64Instruction *insn_ctx) {
    self->literal_insns[self->literal_insn_size++] = insn_ctx;
    // convert the temportary absolute address with offset.
    //    zz_addr_t *temp_address = (zz_addr_t  *)insn_ctx->address;
    //    *temp_address = insn_ctx->pc - self->output->start_pc;
}

// ###### ATTENTION ######
// refer llvm/lib/Target/AArch64/AArch64InstrInfo.td & AArch64InstrFormats.td
// keywords: CmpBranch CBZ CBNZ
static bool arm64_relocator_rewrite_CmpBranch_CBZ_CBNZ(ARM64Relocator *self, const ARM64Instruction *insn_ctx) {
    return true;
};

// ###### ATTENTION ######
// refer ARM64 Architecture Manual
// PAGE: C6-673
static bool arm64_relocator_rewrite_LDR_literal(ARM64Relocator *self, const ARM64Instruction *insn_ctx) {
    uint32_t insn = insn_ctx->insn;
    // TODO: check opc == 10, with signed
    uint32_t imm19  = get_insn_sub(insn, 5, 19);
    uint64_t offset = imm19 << 2;

    zz_addr_t target_address;
    target_address = insn_ctx->pc + offset;
    int Rt_ndx     = get_insn_sub(insn, 0, 4);

    arm64_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    arm64_relocator_register_literal_insn(self, self->output->insns[self->output->insn_size - 1]);
    arm64_writer_put_ldr_reg_reg_offset(self->output, Rt_ndx, Rt_ndx, 0);

    return TRUE;
}

// PAGE: C6-535
static bool arm64_relocator_rewrite_ADR(ARM64Relocator *self, const ARM64Instruction *insn_ctx) {
    uint32_t insn  = insn_ctx->insn;
    uint32_t immhi = get_insn_sub(insn, 5, 19);
    uint32_t immlo = get_insn_sub(insn, 29, 2);
    uint64_t imm   = immhi << 2 | immlo;

    zz_addr_t target_address;
    target_address = insn_ctx->pc + imm;
    int Rt_ndx     = get_insn_sub(insn, 0, 4);

    arm64_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);

    return TRUE;
}

// PAGE: C6-536
static bool arm64_relocator_rewrite_ADRP(ARM64Relocator *self, const ARM64Instruction *insn_ctx) {
    uint32_t insn  = insn_ctx->insn;
    uint32_t immhi = get_insn_sub(insn, 5, 19);
    uint32_t immlo = get_insn_sub(insn, 29, 2);
    // 12 is PAGE-SIZE
    uint64_t imm = immhi << 2 << 12 | immlo << 12;

    zz_addr_t target_address;
    target_address = (insn_ctx->pc & 0xFFFFFFFFFFFFF000) + imm;
    int Rt_ndx     = get_insn_sub(insn, 0, 4);

    arm64_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);

    return TRUE;
}

// PAGE: C6-550
static bool arm64_relocator_rewrite_B(ARM64Relocator *self, const ARM64Instruction *insn_ctx) {
    uint32_t insn  = insn_ctx->insn;
    uint32_t imm26 = get_insn_sub(insn, 0, 26);

    uint64_t offset = imm26 << 2;

    zz_addr_t target_address;
    target_address = insn_ctx->pc + offset;

    arm64_writer_put_ldr_br_reg_address(self->output, ARM64_REG_X17, target_address);
    arm64_relocator_register_literal_insn(self, self->output->insns[self->output->insn_size - 1]);

    return TRUE;
}

// PAGE: C6-560
static bool arm64_relocator_rewrite_BL(ARM64Relocator *self, const ARM64Instruction *insn_ctx) {
    uint32_t insn  = insn_ctx->insn;
    uint32_t imm26 = get_insn_sub(insn, 0, 26);

    uint64_t offset = imm26 << 2;

    zz_addr_t target_address;
    target_address = insn_ctx->pc + offset;

    arm64_writer_put_ldr_blr_b_reg_address(self->output, ARM64_REG_X17, target_address);
    arm64_relocator_register_literal_insn(self, self->output->insns[self->output->insn_size - 1]);
    arm64_writer_put_ldr_br_reg_address(self->output, ARM64_REG_X17, insn_ctx->pc + 4);
    arm64_relocator_register_literal_insn(self, self->output->insns[self->output->insn_size - 1]);

    return TRUE;
}

// 0x000 : b.cond 0x8;

// 0x004 : b 0x14

// 0x008 : ldr x17, [pc, #4]
// 0x00c : br x17
// 0x010 : .long 0x0
// 0x014 : .long 0x0

// 0x018 : remain code

// PAGE: C6-549
static bool arm64_relocator_rewrite_B_cond(ARM64Relocator *self, const ARM64Instruction *insn_ctx) {
    uint32_t insn  = insn_ctx->insn;
    uint32_t imm19 = get_insn_sub(insn, 5, 19);

    uint64_t offset = imm19 << 2;

    zz_addr_t target_address;
    target_address = insn_ctx->pc + offset;

    uint32_t cond = get_insn_sub(insn, 0, 4);

    arm64_writer_put_b_cond_imm(self->output, cond, 0x8);
    arm64_writer_put_b_imm(self->output, 0x14);
    arm64_writer_put_ldr_br_reg_address(self->output, ARM64_REG_X17, target_address);
    arm64_relocator_register_literal_insn(self, self->output->insns[self->output->insn_size - 1]);

    return TRUE;
}

bool arm64_relocator_write_one(ARM64Relocator *self) {
    ARM64Instruction *insn_ctx, **input_insns;
    ARM64RelocatorInstruction *relocator_insn;
    zz_size_t tmp_size;
    relocator_insn = self->relocator_insns + self->relocator_insn_size;
    bool rewritten = FALSE;

    if (self->inpos != self->outpos) {
        input_insns                        = self->input->insns;
        insn_ctx                           = input_insns[self->outpos];
        relocator_insn->origin_insn        = insn_ctx;
        relocator_insn->relocated_insns    = self->output->insns + self->output->insn_size;
        relocator_insn->output_index_start = self->output->insn_size;
        tmp_size                           = self->output->size;
        self->outpos++;
        self->relocator_insn_size++;
    } else
        return FALSE;
    switch (GetARM64InsnType(insn_ctx->insn)) {
    case ARM64_INS_LDR_literal:
        rewritten = arm64_relocator_rewrite_LDR_literal(self, insn_ctx);
        break;
    case ARM64_INS_ADR:
        rewritten = arm64_relocator_rewrite_ADR(self, insn_ctx);
        break;
    case ARM64_INS_ADRP:
        rewritten = arm64_relocator_rewrite_ADRP(self, insn_ctx);
        break;
    case ARM64_INS_B:
        rewritten = arm64_relocator_rewrite_B(self, insn_ctx);
        break;
    case ARM64_INS_BL:
        rewritten = arm64_relocator_rewrite_BL(self, insn_ctx);
        break;
    case ARM64_INS_B_cond:
        rewritten = arm64_relocator_rewrite_B_cond(self, insn_ctx);
        break;
    default:
        rewritten = FALSE;
        break;
    }
    if (!rewritten) {
        arm64_writer_put_bytes(self->output, (char *)&insn_ctx->insn, insn_ctx->size);
    } else {
    }

    relocator_insn->size                = self->output->size - tmp_size;
    relocator_insn->ouput_index_end     = self->output->insn_size;
    relocator_insn->relocated_insn_size = relocator_insn->ouput_index_end - relocator_insn->output_index_start;

    return TRUE;
}