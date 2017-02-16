package fr.unice.polytech.elim.elim9;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

import static fr.unice.polytech.elim.elim9.DataElement.DataKind.*;

/**
 * Created by nathael on 16/02/17.
 */

public class DataElement {
    private static final long MAX_DIFF_TIME = 1000L*60*60*24*7;
    public static final DataElement instance = new DataElement();

    private final Map<DataKind, List<PairDateValue>> dataArrays = new HashMap<>();
    private final Map<String, Double> lastValues = new HashMap<>();

    private DataElement() {
        for(DataKind dk : DataKind.values())
            dataArrays.put(dk, new ArrayList<PairDateValue>());
    }

    private double count(DataKind kind) {
        double value = 0;
        for(PairDateValue d : dataArrays.get(kind)) {
            value += d.value;
        }
        return value;
    }
    /**
     *
     * @return s/100% value
     */
    private double getDischargeActiveAvgTime() {
        double time = count(dischActTime);
        double pct = count(dischActPct)*100;

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }

    /**
     *
     * @return s/100% value
     */
    private double getDischargeInactiveAvgTime() {
        double time = count(dischInactTime);
        double pct = count(dischInactPct)*100;

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }

    /**
     *
     * @return s/100% value
     */
    private double getChargeActiveAvgTime() {
        double time = count(chActTime);
        double pct = count(chActPct)*100;

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }
    /**
     *
     * @return s/100% value
     */
    private double getChargeInactiveAvgTime() {
        double time = count(chInactTime);
        double pct = count(chInactPct)*100;

        if(time == 0 || pct == 0) {
            return -1;
        }
        return time/pct;
    }
    /**
     *
     * @return s/100% value
     */
    private double getAvgRamUsage() {
        if (dataArrays.get(ramUsage).size() == 0)
            return -1;

        double ram = 0;

        for (PairDateValue d : dataArrays.get(ramUsage))
            ram += d.value;

        return ram / dataArrays.get(ramUsage).size();
    }

    public void putDischargeActive(long time, int pct, int pctMax) {
        if ((lastValues.containsKey("time") && lastValues.containsKey("chargeLevel") && lastValues.containsKey("chargeLevelScale"))) {
            List<PairDateValue> timeArray = dataArrays.get(dischActTime);
            List<PairDateValue> pctArray = dataArrays.get(dischActPct);

            timeArray.add(new PairDateValue(time,(double) pct / (double) pctMax));
            timeArray.add(new PairDateValue(time,time / 1000.));

            removeOld(timeArray, time);
            removeOld(pctArray, time);
        }

        lastValues.put("time", time/1000.);
        lastValues.put("chargeLevel", (double) pct);
        lastValues.put("chargeLevelScale", (double) pctMax);
    }
    public void putChargeActive(long time, int pct, int pctMax) {
        if ((lastValues.containsKey("time") && lastValues.containsKey("chargeLevel") && lastValues.containsKey("chargeLevelScale"))) {
            List<PairDateValue> timeArray = dataArrays.get(chActTime);
            List<PairDateValue> pctArray = dataArrays.get(chActPct);

            timeArray.add(new PairDateValue(time,(double)pct/(double)pctMax));
            timeArray.add(new PairDateValue(time,time/1000.));

            removeOld(timeArray, time);
            removeOld(pctArray, time);
        }

        lastValues.put("time", time/1000.);
        lastValues.put("chargeLevel", (double) pct);
        lastValues.put("chargeLevelScale", (double) pctMax);
    }
    public void putDischargeInactive(long time, int pct, int pctMax) {
        if ((lastValues.containsKey("time") && lastValues.containsKey("chargeLevel") && lastValues.containsKey("chargeLevelScale"))) {
            List<PairDateValue> timeArray = dataArrays.get(dischInactTime);
            List<PairDateValue> pctArray = dataArrays.get(dischInactPct);

            timeArray.add(new PairDateValue(time,(double)pct/(double)pctMax));
            timeArray.add(new PairDateValue(time,time/1000.));

            removeOld(timeArray, time);
            removeOld(pctArray, time);
        }

        lastValues.put("time", time/1000.);
        lastValues.put("chargeLevel", (double) pct);
        lastValues.put("chargeLevelScale", (double) pctMax);
    }
    public void putChargeInactive(long time, int pct, int pctMax) {
        if ((lastValues.containsKey("time") && lastValues.containsKey("chargeLevel") && lastValues.containsKey("chargeLevelScale"))) {
            List<PairDateValue> timeArray = dataArrays.get(chInactTime);
            List<PairDateValue> pctArray = dataArrays.get(chInactPct);

            timeArray.add(new PairDateValue(time,(double)pct/(double)pctMax));
            timeArray.add(new PairDateValue(time,time/1000.));

            removeOld(timeArray, time);
            removeOld(pctArray, time);
        }

        lastValues.put("time", time/1000.);
        lastValues.put("chargeLevel", (double) pct);
        lastValues.put("chargeLevelScale", (double) pctMax);
    }

    private void removeOld(List<PairDateValue> array, long date) {
        while(array.size() > 0 && array.get(0).date + MAX_DIFF_TIME < date) {
            array.remove(0);
        }
    }


    public Map<String, Object> toMap() {
        Map<String,Object> ret = new HashMap<>();

        ret.put("chargeActive", getChargeActiveAvgTime());
        ret.put("chargeInactive", getChargeInactiveAvgTime());
        ret.put("dischargeActive", getDischargeActiveAvgTime());
        ret.put("dischargeInactive", getDischargeInactiveAvgTime());
        ret.put("ramUsage", getAvgRamUsage());

        return ret;
    }

    public static void save(final String SAVE_FILE_NAME) {
        File f = new File(SAVE_FILE_NAME);
        try {
            if (!f.exists() || f.delete())
                f.createNewFile();

            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(DataElement.instance.dataArrays);
            oos.close();
            fos.close();
        } catch (IOException e) {
            Log.e("DataElement", "Save:"+e.getMessage(), e);
        }
    }

    public static void load(final String SAVE_FILE_NAME) {
        File f = new File(SAVE_FILE_NAME);
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Map<DataKind, List<PairDateValue>> map = (Map<DataKind, List<PairDateValue>>) ois.readObject();

            if (map != null) {
                instance.dataArrays.clear();
                instance.dataArrays.putAll(map);
            }

            ois.close();
            fis.close();
        } catch (FileNotFoundException ignored) {
        } catch (IOException | ClassNotFoundException e) {
            Log.e("DataElement", "Load:"+e.getMessage(), e);
        }
    }


    enum DataKind {
        dischActTime,
        dischInactTime,
        chActTime,
        chInactTime,
        dischActPct,
        dischInactPct,
        chActPct,
        chInactPct,
        ramUsage
    }

    private class PairDateValue {
        private final long date;
        private final double value;

        private PairDateValue(long date, double value) {
            this.date = date;
            this.value = value;
        }
    }
}
