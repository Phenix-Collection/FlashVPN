package com.polestar.superclone.reward;
import android.graphics.Bitmap;

public class FastBlur {
    public FastBlur() {
        super();
    }

    public static Bitmap blur(Bitmap arg44, int arg45, boolean arg46) {
        int v33;
        int v36;
        int[] v35;
        int v25;
        int v41;
        int v42;
        int v43;
        int v38;
        int v39;
        int v40;
        int v19;
        int v20;
        int v21;
        Bitmap v8 = arg46 ? arg44 : arg44.copy(arg44.getConfig(), true);
        if(arg45 < 1) {
            return null;
        }

        int v9 = v8.getWidth();
        int v10 = v8.getHeight();
        int[] v11 = new int[v9 * v10];
        v8.getPixels(v11, 0, v9, 0, 0, v9, v10);
        int v12 = v9 - 1;
        int v13 = v10 - 1;
        int v14 = v9 * v10;
        int v15 = arg45 + arg45 + 1;
        int[] v16 = new int[v14];
        int[] v17 = new int[v14];
        int[] v18 = new int[v14];
        int[] v29 = new int[Math.max(v9, v10)];
        int v30 = v15 + 1 >> 1;
        v30 *= v30;
        int[] v31 = new int[v30 * 256];
        int v24;
        for(v24 = 0; v24 < v30 * 256; ++v24) {
            v31[v24] = v24 / v30;
        }

        int v27 = 0;
        int v28 = 0;
        int[][] v32 = new int[v15][3];
        int v37 = arg45 + 1;
        int v23;
        for(v23 = 0; v23 < v10; ++v23) {
            v21 = 0;
            v20 = 0;
            v19 = 0;
            v40 = 0;
            v39 = 0;
            v38 = 0;
            v43 = 0;
            v42 = 0;
            v41 = 0;
            for(v24 = -arg45; v24 <= arg45; ++v24) {
                v25 = v11[Math.min(v12, Math.max(v24, 0)) + v27];
                v35 = v32[v24 + arg45];
                v35[0] = (16711680 & v25) >> 16;
                v35[1] = (65280 & v25) >> 8;
                v35[2] = v25 & 255;
                v36 = v37 - Math.abs(v24);
                v19 += v35[0] * v36;
                v20 += v35[1] * v36;
                v21 += v35[2] * v36;
                if(v24 > 0) {
                    v41 += v35[0];
                    v42 += v35[1];
                    v43 += v35[2];
                }
                else {
                    v38 += v35[0];
                    v39 += v35[1];
                    v40 += v35[2];
                }
            }

            v33 = arg45;

            for(int v22 = 0; v22 < v9; ++v22) {
                v16[v27] = v31[v19];
                v17[v27] = v31[v20];
                v18[v27] = v31[v21];
                v19 -= v38;
                v20 -= v39;
                v21 -= v40;
                v35 = v32[(v33 - arg45 + v15) % v15];
                v38 -= v35[0];
                v39 -= v35[1];
                v40 -= v35[2];
                if(v23 == 0) {
                    v29[v22] = Math.min(v22 + arg45 + 1, v12);
                }

                v25 = v11[v29[v22] + v28];
                v35[0] = (16711680 & v25) >> 16;
                v35[1] = (65280 & v25) >> 8;
                v35[2] = v25 & 255;
                v41 += v35[0];
                v42 += v35[1];
                v43 += v35[2];
                v19 += v41;
                v20 += v42;
                v21 += v43;
                v33 = (v33 + 1) % v15;
                v35 = v32[v33 % v15];
                v38 += v35[0];
                v39 += v35[1];
                v40 += v35[2];
                v41 -= v35[0];
                v42 -= v35[1];
                v43 -= v35[2];
                ++v27;
            }

            v28 += v9;
        }

        for(int v22 = 0; v22 < v9; ++v22) {
            v21 = 0;
            v20 = 0;
            v19 = 0;
            v40 = 0;
            v39 = 0;
            v38 = 0;
            v43 = 0;
            v42 = 0;
            v41 = 0;
            int v26 = -arg45 * v9;
            for(v24 = -arg45; v24 <= arg45; ++v24) {
                v27 = Math.max(0, v26) + v22;
                v35 = v32[v24 + arg45];
                v35[0] = v16[v27];
                v35[1] = v17[v27];
                v35[2] = v18[v27];
                v36 = v37 - Math.abs(v24);
                v19 += v16[v27] * v36;
                v20 += v17[v27] * v36;
                v21 += v18[v27] * v36;
                if(v24 > 0) {
                    v41 += v35[0];
                    v42 += v35[1];
                    v43 += v35[2];
                }
                else {
                    v38 += v35[0];
                    v39 += v35[1];
                    v40 += v35[2];
                }

                if(v24 < v13) {
                    v26 += v9;
                }
            }

            v27 = v22;
            v33 = arg45;
            for(v23 = 0; v23 < v10; ++v23) {
                v11[v27] = v11[v27] & -16777216 | v31[v19] << 16 | v31[v20] << 8 | v31[v21];
                v19 -= v38;
                v20 -= v39;
                v21 -= v40;
                v35 = v32[(v33 - arg45 + v15) % v15];
                v38 -= v35[0];
                v39 -= v35[1];
                v40 -= v35[2];
                if(v22 == 0) {
                    v29[v23] = Math.min(v23 + v37, v13) * v9;
                }

                v25 = v22 + v29[v23];
                v35[0] = v16[v25];
                v35[1] = v17[v25];
                v35[2] = v18[v25];
                v41 += v35[0];
                v42 += v35[1];
                v43 += v35[2];
                v19 += v41;
                v20 += v42;
                v21 += v43;
                v33 = (v33 + 1) % v15;
                v35 = v32[v33];
                v38 += v35[0];
                v39 += v35[1];
                v40 += v35[2];
                v41 -= v35[0];
                v42 -= v35[1];
                v43 -= v35[2];
                v27 += v9;
            }
        }

        v8.setPixels(v11, 0, v9, 0, 0, v9, v10);
        return v8;
    }
}
