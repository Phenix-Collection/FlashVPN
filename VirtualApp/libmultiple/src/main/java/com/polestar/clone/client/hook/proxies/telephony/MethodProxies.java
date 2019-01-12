package com.polestar.clone.client.hook.proxies.telephony;

import android.os.Bundle;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

import com.polestar.clone.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.polestar.clone.client.hook.base.ReplaceLastPkgMethodProxy;
import com.polestar.clone.client.hook.base.StaticMethodProxy;
import com.polestar.clone.client.ipc.VirtualLocationManager;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.helper.utils.marks.FakeDeviceMark;
import com.polestar.clone.helper.utils.marks.FakeLocMark;
import com.polestar.clone.remote.vloc.VCell;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 */
@SuppressWarnings("ALL")
class MethodProxies {

    @FakeDeviceMark("fake device id.")
    static class GetDeviceId extends ReplaceLastPkgMethodProxy {

        public GetDeviceId() {
            super("getDeviceId");
            VLog.d("JJJJ", this.getClass().getName());
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return getDeviceInfo().deviceId;
        }
    }

    @FakeLocMark("cell location")
    static class GetCellLocation extends ReplaceCallingPkgMethodProxy {

        public GetCellLocation() {
            super("getCellLocation");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                VCell cell = VirtualLocationManager.get().getCell(getAppUserId(), getAppPkg());
                if (cell != null) {
                    return getCellLocationInternal(cell);
                }
            }
            return super.call(who, method, args);
        }
    }

    static class GetAllCellInfoUsingSubId extends ReplaceCallingPkgMethodProxy {

        public GetAllCellInfoUsingSubId() {
            super("getAllCellInfoUsingSubId");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                return null;
            }
            return super.call(who, method, args);
        }
    }

    @FakeLocMark("cell location")
    static class GetAllCellInfo extends ReplaceCallingPkgMethodProxy {

        public GetAllCellInfo() {
            super("getAllCellInfo");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                List<VCell> cells = VirtualLocationManager.get().getAllCell(getAppUserId(), getAppPkg());
                if (cells != null) {
                    List<CellInfo> result = new ArrayList<CellInfo>();
                    for (VCell cell : cells) {
                        result.add(createCellInfo(cell));
                    }
                    return result;
                }

            }
            try {
                return super.call(who, method, args);
            }catch (Throwable ex) {
                return new ArrayList<CellInfo>(0);
            }
        }
    }

    @FakeLocMark("neb cell location")
    static class GetNeighboringCellInfo extends ReplaceCallingPkgMethodProxy {

        public GetNeighboringCellInfo() {
            super("getNeighboringCellInfo");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                List<VCell> cells = VirtualLocationManager.get().getNeighboringCell(getAppUserId(), getAppPkg());
                if (cells != null) {
                    List<NeighboringCellInfo> infos = new ArrayList<>();
                    for (VCell cell : cells) {
                        NeighboringCellInfo info = new NeighboringCellInfo();
                        mirror.android.telephony.NeighboringCellInfo.mLac.set(info, cell.lac);
                        mirror.android.telephony.NeighboringCellInfo.mCid.set(info, cell.cid);
                        mirror.android.telephony.NeighboringCellInfo.mRssi.set(info, 6);
                        infos.add(info);
                    }
                    return infos;
                }
            }
            return super.call(who, method, args);
        }
    }

    private static Bundle getCellLocationInternal(VCell cell) {
        if (cell != null) {
            Bundle cellData = new Bundle();
            if (cell.type == 2) {
                try {
                    CdmaCellLocation cellLoc = new CdmaCellLocation();
                    cellLoc.setCellLocationData(cell.baseStationId, Integer.MAX_VALUE, Integer.MAX_VALUE, cell.systemId, cell.networkId);
                    cellLoc.fillInNotifierBundle(cellData);
                } catch (Throwable e) {
                    cellData.putInt("baseStationId", cell.baseStationId);
                    cellData.putInt("baseStationLatitude", Integer.MAX_VALUE);
                    cellData.putInt("baseStationLongitude", Integer.MAX_VALUE);
                    cellData.putInt("systemId", cell.systemId);
                    cellData.putInt("networkId", cell.networkId);
                }
            } else {
                try {
                    GsmCellLocation cellLoc = new GsmCellLocation();
                    cellLoc.setLacAndCid(cell.lac, cell.cid);
                    cellLoc.fillInNotifierBundle(cellData);
                } catch (Throwable e) {
                    cellData.putInt("lac", cell.lac);
                    cellData.putInt("cid", cell.cid);
                    cellData.putInt("psc", cell.psc);
                }
            }
            return cellData;
        }
        return null;
    }


    private static CellInfo createCellInfo(VCell cell) {
        if (cell.type == 2) { // CDMA
            CellInfoCdma cdma = mirror.android.telephony.CellInfoCdma.ctor.newInstance();
            CellIdentityCdma identityCdma = mirror.android.telephony.CellInfoCdma.mCellIdentityCdma.get(cdma);
            CellSignalStrengthCdma strengthCdma = mirror.android.telephony.CellInfoCdma.mCellSignalStrengthCdma.get(cdma);
            mirror.android.telephony.CellIdentityCdma.mNetworkId.set(identityCdma, cell.networkId);
            mirror.android.telephony.CellIdentityCdma.mSystemId.set(identityCdma, cell.systemId);
            mirror.android.telephony.CellIdentityCdma.mBasestationId.set(identityCdma, cell.baseStationId);
            mirror.android.telephony.CellSignalStrengthCdma.mCdmaDbm.set(strengthCdma, -74);
            mirror.android.telephony.CellSignalStrengthCdma.mCdmaEcio.set(strengthCdma, -91);
            mirror.android.telephony.CellSignalStrengthCdma.mEvdoDbm.set(strengthCdma, -64);
            mirror.android.telephony.CellSignalStrengthCdma.mEvdoSnr.set(strengthCdma, 7);
            return cdma;
        } else { // GSM
            CellInfoGsm gsm = mirror.android.telephony.CellInfoGsm.ctor.newInstance();
            CellIdentityGsm identityGsm = mirror.android.telephony.CellInfoGsm.mCellIdentityGsm.get(gsm);
            CellSignalStrengthGsm strengthGsm = mirror.android.telephony.CellInfoGsm.mCellSignalStrengthGsm.get(gsm);
            mirror.android.telephony.CellIdentityGsm.mMcc.set(identityGsm, cell.mcc);
            mirror.android.telephony.CellIdentityGsm.mMnc.set(identityGsm, cell.mnc);
            mirror.android.telephony.CellIdentityGsm.mLac.set(identityGsm, cell.lac);
            mirror.android.telephony.CellIdentityGsm.mCid.set(identityGsm, cell.cid);
            mirror.android.telephony.CellSignalStrengthGsm.mSignalStrength.set(strengthGsm, 20);
            mirror.android.telephony.CellSignalStrengthGsm.mBitErrorRate.set(strengthGsm, 0);
            return gsm;
        }
    }



    static class GetDeviceIdForPhone extends GetDeviceId {
        GetDeviceIdForPhone() {
            super();
        }

        public String getMethodName() {
            return "getDeviceIdForPhone";
        }
    }

    static class GetDeviceIdForSubscriber extends GetDeviceId {
        GetDeviceIdForSubscriber() {
            super();
        }

        public String getMethodName() {
            return "getDeviceIdForSubscriber";
        }
    }

    static class GetIccSerialNumber extends ReplaceLastPkgMethodProxy {
        public GetIccSerialNumber() {
            super("getIccSerialNumber");
        }

        public Object call(Object arg3, Method arg4, Object[] arg5) {
            try {
                if (getDeviceInfo().iccId == null) {
                    return super.call(arg3, arg4, arg5);
                } else {
                    return getDeviceInfo().iccId;
                }
            }catch (Throwable ex) {
                return "";
            }
        }
    }

    static class GetIccSerialNumberForSubscriber extends GetIccSerialNumber {
        GetIccSerialNumberForSubscriber() {
            super();
        }

        public String getMethodName() {
            return "getIccSerialNumberForSubscriber";
        }
    }

    static class GetImeiForSubscriber extends GetDeviceId {
        GetImeiForSubscriber() {
            super();
        }

        public String getMethodName() {
            return "getImeiForSubscriber";
        }
    }

    static class GetImeiForSlot extends GetDeviceId {
        GetImeiForSlot() {
            super();
        }

        public String getMethodName() {
            return "getImeiForSlot";
        }
    }

    static class GetMeidForSlot extends GetDeviceId {
        GetMeidForSlot() {
            super();
        }

        public String getMethodName() {
            return "getMeidForSlot";
        }
    }
}
