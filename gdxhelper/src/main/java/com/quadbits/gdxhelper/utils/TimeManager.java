/*
 * Copyright (c) 2015 Quadbits SLU
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quadbits.gdxhelper.utils;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.Calendar;

/**
 *
 */
public class TimeManager {
    /**
     * An enum type for the different periods of the day, depending on the sun's positions
     */
    public enum DayPeriod {
        TWILIGHT_PRE_SUNRISE,
        TWILIGHT_POST_SUNRISE,
        PRE_MIDDAY,
        POST_MIDDAY,
        TWILIGHT_PRE_SUNSET,
        TWILIGHT_POST_SUNSET,
        PRE_MIDNIGHT,
        POST_MIDNIGHT
    }

    /**
     * A helper class for holding day-period data, such as normalized time of beginning, ending,
     * interval width, etc.
     */
    public static class DayInterval implements Comparable<DayInterval>, Pool.Poolable {
        DayPeriod id;
        float tBegin;
        float tEnd;
        float tWidth;
        float tExtraPreWidth;
        float tExtraPostWidth;

        @Override
        public void reset() {
            tBegin = 0;
            tEnd = 0;
            tWidth = 0;
            tExtraPreWidth = 0;
            tExtraPostWidth = 0;
        }

        public void init(DayPeriod id, float tBegin, float tEnd) {
            this.id = id;
            this.tBegin = tBegin;
            this.tEnd = tEnd;
            this.tWidth = tEnd - tBegin;
        }

        @Override
        public int compareTo(DayInterval dayInterval) {
            return (int) Math.signum(this.tBegin - dayInterval.tBegin);
        }

        public boolean contains(float absoluteT) {
            if (absoluteT < tBegin || absoluteT > tEnd) {
                return false;
            }
            return true;
        }

        public float calculateRelativeT(float absoluteT) {
            if (!contains(absoluteT)) {
                return -1;
            }

            return (tExtraPreWidth + absoluteT - tBegin) /
                    (tExtraPreWidth + tWidth + tExtraPostWidth);
        }

        public float calculateAbsoluteT(float relativeT) {
            if (relativeT < 0 || relativeT > 1) {
                return -1;
            }

            float relativeTBegin = tExtraPreWidth / (tExtraPreWidth + tWidth + tExtraPostWidth);
            float relativeTEnd =
                    (tExtraPreWidth + tWidth) / (tExtraPreWidth + tWidth + tExtraPostWidth);

            if (relativeT < relativeTBegin || relativeT > relativeTEnd) {
                return -1;
            }

            float rescaledRelativeT =
                    (relativeT - relativeTBegin) / (relativeTEnd - relativeTBegin);
            return tBegin + rescaledRelativeT * tWidth;
        }

    }

    long periodMillis;

    long unnormalizedT;
    float t;
    DayPeriod dayPeriod;
    float tPeriod;
    float normalizedDayPeriodWidth;
    float tSun;
    float tNight;
    float tMoon;
    Array<DayInterval> dayIntervals;
    Pool<DayInterval> dayIntervalPool;

    float tSunRise;
    float tSunSet;
    float tMoonRise;
    float tMoonSet;
    float tTwilight;
    float tMidday;
    float tMidnight;
    float tPreDusk; // tSunRise - tTwilight
    float tPostDusk; // tSunRise + tTwilight
    float tPreDawn; // tSunSet - tTwilight
    float tPostDawn; // tSunSet + tTwilight

    float sunTravelWidth;
    float moonTravelWidth;

    public enum TimeMode {
        NORMAL, FAST_FORWARD
    }

    TimeMode timeMode;
    long maxUnnormalizedTimeDiff;
    long ffTimeInit;
    long ffTimeLength;
    long ffAnimTimeInit;
    long ffAnimTimeLength;

    public static final long ONE_DAY_PERIOD_IN_MILLIS = 1000 * 60 * 60 * 24;
    public static final int EPSILON_DIFF_MILLIS = 1000;
    public static final int FF_ANIMATION_MIN_LENGTH = 2000;
    public static final int FF_ANIMATION_MAX_LENGTH = 7000;
    /**
     * The default value of twilight is the real duration of a twilight in the real world,
     * the time it takes since the first lights of the sun appear until the sun rises. It
     * corresponds with the duration of a 9º rotation of the Earth, i.e.,
     * 9/360 = 0.025, in its normalized form.
     */
    public static final float TTWILIGHT_DEFAULT_VALUE = 0.025f;

    public TimeManager(float tSunRise, float tSunSet) {
        // Initialize structures
        dayIntervals = null;
        dayIntervalPool = new Pool<DayInterval>() {
            @Override
            protected DayInterval newObject() {
                return new DayInterval();
            }
        };
        this.periodMillis = ONE_DAY_PERIOD_IN_MILLIS;
        timeMode = TimeMode.NORMAL;

        boolean updateTime = false;

        // Set sun times
        setSunTimes(tSunRise, tSunSet, TTWILIGHT_DEFAULT_VALUE, updateTime);

        // Set default values for moon times
        setMoonTimes(tPostDawn, tPreDusk, updateTime);

        // Synch internal time with the machine's real time
        syncTime();
    }

    /**
     * @param tSunRise
     *         normalized time of the sunrise (in [0, 1])
     * @param tSunSet
     *         normalized time of the sunset (in [0, 1])
     */
    public void setSunTimes(float tSunRise, float tSunSet) {
        setSunTimes(tSunRise, tSunSet, tTwilight);
    }

    public void setSunTimes(float tSunRise, float tSunSet, float tTwilight) {
        setSunTimes(tSunRise, tSunSet, tTwilight, true);
    }

    private void setSunTimes(float tSunRise, float tSunSet, float tTwilight, boolean updateTime) {
        // Parameter checking
        if (tSunRise < 0 || tSunRise > 1) {
            throw new IllegalArgumentException("tSunRise = " + tSunRise + ", out of [0,1] range");
        }
        if (tSunSet < 0 || tSunSet > 1) {
            throw new IllegalArgumentException("tSunSet = " + tSunSet + ", out of [0,1] range");
        }
        if (tTwilight < 0 || tTwilight > 1) {
            throw new IllegalArgumentException("tTwilight = " + tTwilight + ", out of [0,1] range");
        }

        // Provided values
        this.tSunRise = tSunRise;
        this.tSunSet = tSunSet;
        this.tTwilight = tTwilight;

        // Calculated values
        if (tSunRise < tSunSet) {
            sunTravelWidth = tSunSet - tSunRise;
        } else {
            sunTravelWidth = 1 - (tSunRise - tSunSet);
        }
        tMidday = (tSunRise + sunTravelWidth / 2) % 1;
        tMidnight = (tSunSet + (1 - sunTravelWidth) / 2) % 1;
        tPreDusk = (tSunRise - tTwilight) % 1;
        tPostDusk = (tSunRise + tTwilight) % 1;
        tPreDawn = (tSunSet - tTwilight) % 1;
        tPostDawn = (tSunSet + tTwilight) % 1;

        boolean recalculateT = false;

        // Create day intervals
        if (dayIntervals == null) {
            dayIntervals = new Array<DayInterval>();
        } else {
            recalculateT = true;

            // Free intervals
            for (DayInterval dayInterval : dayIntervals) {
                dayIntervalPool.free(dayInterval);
            }
            dayIntervals.clear();
        }
        createDayInterval(DayPeriod.TWILIGHT_PRE_SUNRISE, tPreDusk, tSunRise);
        createDayInterval(DayPeriod.TWILIGHT_POST_SUNRISE, tSunRise, tPostDusk);
        createDayInterval(DayPeriod.PRE_MIDDAY, tPostDusk, tMidday);
        createDayInterval(DayPeriod.POST_MIDDAY, tMidday, tPreDawn);
        createDayInterval(DayPeriod.TWILIGHT_PRE_SUNSET, tPreDawn, tSunSet);
        createDayInterval(DayPeriod.TWILIGHT_POST_SUNSET, tSunSet, tPostDawn);
        createDayInterval(DayPeriod.PRE_MIDNIGHT, tPostDawn, tMidnight);
        createDayInterval(DayPeriod.POST_MIDNIGHT, tMidnight, tPreDusk);
        dayIntervals.sort();

        // Update time structures?
        if (updateTime) {
            // Recalculate t?
            if (recalculateT) {
                // Previous active day period and its relativeT are stored in the TimeManager's
                // dayPeriod and tPeriod properties
                for (DayInterval dayInterval : dayIntervals) {
                    if (dayInterval.id == dayPeriod) {
                        float recalculatedT = dayInterval.calculateAbsoluteT(tPeriod);
                        if (recalculatedT >= 0) {
                            setT(recalculatedT);
                            break;
                        }
                    }
                }
            }

            updateTime();
        }
    }

    /**
     * Internal method for adding a day interval to the array of intervals,
     * taking into account intervals that need to be splitted.
     *
     * @param id
     * @param tBegin
     * @param tEnd
     */
    protected void createDayInterval(DayPeriod id, float tBegin, float tEnd) {
        if (tBegin < tEnd) {
            DayInterval dayInterval = dayIntervalPool.obtain();
            dayInterval.init(id, tBegin, tEnd);
            dayIntervals.add(dayInterval);
        } else {
            DayInterval dayInterval1 = dayIntervalPool.obtain();
            dayInterval1.init(id, tBegin, 1);
            DayInterval dayInterval2 = dayIntervalPool.obtain();
            dayInterval2.init(id, 0, tEnd);
            dayInterval1.tExtraPostWidth = dayInterval2.tWidth;
            dayInterval2.tExtraPreWidth = dayInterval1.tWidth;
            dayIntervals.add(dayInterval1);
            dayIntervals.add(dayInterval2);
        }
    }

    public void setMoonTimes(float tMoonRise, float tMoonSet) {
        setMoonTimes(tMoonRise, tMoonSet, true);
    }

    private void setMoonTimes(float tMoonRise, float tMoonSet, boolean updateTime) {
        this.tMoonRise = tMoonRise;
        this.tMoonSet = tMoonSet;
        if (tMoonRise < tMoonSet) {
            moonTravelWidth = tMoonSet - tMoonRise;
        } else {
            moonTravelWidth = 1 - (tMoonRise - tMoonSet);
        }

        // Update time structures
        if (updateTime) {
            updateTime();
        }
    }

    /**
     * Synchronizes internal time according to the machine's time of day (milliseconds
     * after midnight). The value is normalized according to the 'periodMillis' property (see
     * {@link #setPeriodMillis(long)}). This method updates the internal time <b>immediately</b>,
     * without switching to fast-forward mode.
     */
    public void syncTime() {
        setUnnormalizedT(getMillisSinceMidnight());
    }

    /**
     * Updates the time passed since the last update. If the time passed is higher than {@link
     * #getMaxUnnormalizedTimeDiff()}, the time manager switches to fast-forward mode.
     */
    public void updateTime() {
        long currentUnnormalizedT = getMillisSinceMidnight();
        long unnormalizedTimeDiff = getTimeDiffMillis(unnormalizedT, currentUnnormalizedT);

        updateTime(unnormalizedTimeDiff);
    }

    public void updateTime(long unnormalizedTimeDiff) {
        long currentUnnormalizedT = unnormalizedT + unnormalizedTimeDiff;

        // Switch to FAST_FORWARD mode?
        if (switchToFastForwardMode(unnormalizedTimeDiff)) {
            if (timeMode == TimeMode.NORMAL) {
                ffTimeInit = unnormalizedT;
                ffAnimTimeInit = currentUnnormalizedT;
                ffTimeLength = unnormalizedTimeDiff;
                ffAnimTimeLength = FF_ANIMATION_MIN_LENGTH +
                        (long) (((float) unnormalizedTimeDiff / (float) periodMillis) *
                                FF_ANIMATION_MAX_LENGTH);
            }
            timeMode = TimeMode.FAST_FORWARD;
        }

        // Switch to NORMAL mode?
        long ffAnimTimeDiff = 0;
        if (timeMode == TimeMode.FAST_FORWARD) {
            ffAnimTimeDiff = getTimeDiffMillis(ffAnimTimeInit, currentUnnormalizedT);
            if (ffAnimTimeDiff > ffAnimTimeLength) {
                timeMode = TimeMode.NORMAL;
            }
        }

        // time mode
        switch (timeMode) {
            case NORMAL:
                // Set time
                setUnnormalizedT(currentUnnormalizedT);
                break;

            case FAST_FORWARD:
                // Calculate the % of animation time passed
                float ffPerc = (float) ffAnimTimeDiff / (float) ffAnimTimeLength;
                if (ffPerc > 1) {
                    ffPerc = 1; // Clamp
                }

                // Use an interpolation function to update fast-forward time
                //                long ffUnormalizedT = (long) Interpolation.linear
                //                        .apply(ffTimeInit, ffTimeInit + ffTimeLength, ffPerc);
                //                long ffUnormalizedT = (long) Interpolation.pow5Out
                //                        .apply(ffTimeInit, ffTimeInit + ffTimeLength, ffPerc);
                long ffUnormalizedT = (long) Interpolation.pow2
                        .apply(ffTimeInit, ffTimeInit + ffTimeLength, ffPerc);

                // Set time
                setUnnormalizedT(ffUnormalizedT);
        }
    }

    protected boolean switchToFastForwardMode(float unnormalizedTimeDiff) {
        if (periodMillis != ONE_DAY_PERIOD_IN_MILLIS) {
            return false;
        }

        return unnormalizedTimeDiff > maxUnnormalizedTimeDiff + EPSILON_DIFF_MILLIS;
    }

    /**
     * Calculates the time difference in milliseconds between timeInit and timeEnd,
     * where timeInit is assumed to be an earlier instant in time than timeEnd. Both timeInit and
     * timeEnd must be expressed in milliseconds-after-midnight, and are therefore values in the
     * interval [0, ONE_DAY_PERIOD_IN_MILLIS]. If timeEnd < timeInit, day-wrapping is assumed.
     *
     * @param timeInit
     * @param timeEnd
     *
     * @return
     */
    public static long getTimeDiffMillis(long timeInit, long timeEnd) {
        long timeDiffMillis;
        if (timeEnd >= timeInit) {
            timeDiffMillis = timeEnd - timeInit;
        } else {
            timeDiffMillis = TimeManager.ONE_DAY_PERIOD_IN_MILLIS - (timeInit - timeEnd);
        }

        return timeDiffMillis;
    }

    /**
     * Returns the number of milliseconds since midnight using the machine's local time.
     *
     * @return
     */
    public static long getMillisSinceMidnight() {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return (now - c.getTimeInMillis());
    }

    /**
     * Returns the number of milliseconds since midnight using the provided time.
     *
     * @return
     */
    public static long getMillisSinceMidnight(int hour, int minute, int second, int millis) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Out of bounds value for 'hour' (" + hour + ")");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Out of bounds value for 'minute' (" + minute + ")");
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException("Out of bounds value for 'second' (" + second + ")");
        }
        if (millis < 0 || millis > 999) {
            throw new IllegalArgumentException("Out of bounds value for 'millis' (" + millis + ")");
        }

        return (millis + 1000 * (second + 60 * (minute + 60 * hour)));
    }

    public static float getNormalizedTime(int hour, int minute, int second, int millis,
                                          float periodMillis) {
        float time = getMillisSinceMidnight(hour, minute, second, millis);
        time = time % periodMillis;
        return time / periodMillis;
    }

    public static long getUnnormalizedTime(float t, float periodMillis) {
        return (long) (t * periodMillis);
    }

    /**
     * Sets the internal value of the dayPeriod according to the provided normalized time 't' and
     * the different day periods defined by sunrise, sunset and twilight times. Also updates the
     * 'tPeriod', a normalized time relative to the current dayPeriod.
     *
     * @param t
     */
    public void updateDayPeriod(float t) {
        for (DayInterval dayInterval : dayIntervals) {
            float tPeriodTmp = dayInterval.calculateRelativeT(t);
            if (tPeriodTmp >= 0) {
                dayPeriod = dayInterval.id;
                tPeriod = tPeriodTmp;
                normalizedDayPeriodWidth = dayInterval.tExtraPreWidth + dayInterval.tWidth +
                        dayInterval.tExtraPostWidth;
                break;
            }
        }
    }

    /**
     * Calculate the Sun's relative normalized time according to the global absolute time 't'
     *
     * @param t
     *
     * @return
     */
    public float calculateTSun(float t) {
        if (tSunRise < tSunSet) {
            if (t < tSunRise || t > tSunSet) {
                return -1;
            }
            return (t - tSunRise) / sunTravelWidth;
        } else {
            if (t > tSunSet && t < tSunRise) {
                return -1;
            }
            if (t >= tSunRise) {
                return (t - tSunRise) / sunTravelWidth;
            } else {
                return (t + (1 - tSunRise)) / sunTravelWidth;
            }
        }
    }

    /**
     * Calculate the night's relative normalized time according to the global absolute time 't'.
     *
     * @param t
     *
     * @return
     */
    public float calculateTNight(float t) {
        if (tSunRise < tSunSet) {
            if (t > tSunRise && t < tSunSet) {
                return -1;
            }
            if (t >= tSunSet) {
                return (t - tSunSet) / (1 - sunTravelWidth);
            } else {
                return (t + (1 - tSunSet)) / (1 - sunTravelWidth);
            }
        } else {
            if (t < tSunSet || t > tSunRise) {
                return -1;
            }
            return (t - tSunSet) / (1 - sunTravelWidth);
        }
    }

    /**
     * Calculate the Moon's relative normalized time according to the global absolute time 't'
     *
     * @param t
     *
     * @return
     */
    public float calculateTMoon(float t) {
        if (tMoonRise < tMoonSet) {
            if (t < tMoonRise || t > tMoonSet) {
                return -1;
            }
            return (t - tMoonRise) / moonTravelWidth;
        } else {
            if (t > tMoonSet && t < tMoonRise) {
                return -1;
            }
            if (t > tMoonRise) {
                return (t - tMoonRise) / moonTravelWidth;
            } else {
                return (t + (1 - tMoonRise)) / moonTravelWidth;
            }
        }
    }

    public float getT() {
        return t;
    }

    protected void setT(float t) {
        this.t = t;
        this.unnormalizedT = (long) (t * periodMillis);

        updateDayPeriod(t);
        tSun = calculateTSun(t);
        tNight = calculateTNight(t);
        tMoon = calculateTMoon(t);
    }

    public long getUnnormalizedT() {
        return unnormalizedT;
    }

    protected void setUnnormalizedT(long unnormalizedT) {
        this.unnormalizedT = unnormalizedT % ONE_DAY_PERIOD_IN_MILLIS;

        // IMPORTANT!! Perform the following two operations (mod and div) separately; else,
        // round errors may happen
        float tmpT = unnormalizedT % periodMillis;
        t = tmpT / periodMillis;

        updateDayPeriod(t);
        tSun = calculateTSun(t);
        tNight = calculateTNight(t);
        tMoon = calculateTMoon(t);
    }

    public float getTSunRise() {
        return tSunRise;
    }

    public float getTSunSet() {
        return tSunSet;
    }

    public float getTTwilight() {
        return tTwilight;
    }

    public float getTMidday() {
        return tMidday;
    }

    public float getTMidnight() {
        return tMidnight;
    }

    public float getTPreDusk() {
        return tPreDusk;
    }

    public float getTPostDusk() {
        return tPostDusk;
    }

    public float getTPreDawn() {
        return tPreDawn;
    }

    public float getTPostDawn() {
        return tPostDawn;
    }

    public float getTMoonRise() {
        return tMoonRise;
    }

    public float getTMoonSet() {
        return tMoonSet;
    }

    public DayPeriod getPeriod() {
        return dayPeriod;
    }

    public float getTPeriod() {
        return tPeriod;
    }

    public float getNormalizedDayPeriodWidth() {
        return normalizedDayPeriodWidth;
    }

    public long getUnnormalizedDayPeriodWidth() {
        return (long) Math.ceil(normalizedDayPeriodWidth * periodMillis);
    }

    public float getSunTravelWidth() {
        return sunTravelWidth;
    }

    public float getTSun() {
        return tSun;
    }

    public float getTNight() {
        return tNight;
    }

    public float getTMoon() {
        return tMoon;
    }

    public float getMoonTravelWidth() {
        return moonTravelWidth;
    }

    public long getPeriodMillis() {
        return periodMillis;
    }

    public void setPeriodMillis(long periodMillis) {
        this.periodMillis = periodMillis;
        // Force normal mode if period is not a whole day
        if (periodMillis != ONE_DAY_PERIOD_IN_MILLIS) {
            timeMode = TimeMode.NORMAL;
        }
        updateTime();
    }

    public TimeMode getTimeMode() {
        return timeMode;
    }

    public long getMaxUnnormalizedTimeDiff() {
        return maxUnnormalizedTimeDiff;
    }

    public void setMaxUnnormalizedTimeDiff(long maxUnnormalizedTimeDiff) {
        this.maxUnnormalizedTimeDiff = maxUnnormalizedTimeDiff;
    }

    public interface TimeFuzzyPeriodMembershipFunction {
        /**
         * Determines if a certain period of the day is going on at the current time,
         * as expressed by the corresponding time manager passed as argument. This is expressed
         * by means of a membership function that returns a value in the range [0, 1],
         * where 0 means that the period is not going on, 1 means fully going on,
         * and any value in between expresses a transition between not going on and going on.
         *
         * @param timeManager
         *         The time manager that will determine the current time
         *
         * @return a value in the interval [0, 1]
         */
        float evaluate(TimeManager timeManager);
    }

    public static TimeFuzzyPeriodMembershipFunction isDay =
            new TimeFuzzyPeriodMembershipFunction() {

                @Override
                public float evaluate(TimeManager timeManager) {
                    switch (timeManager.getPeriod()) {
                        case PRE_MIDDAY:
                        case POST_MIDDAY:
                            return 1;

                        case TWILIGHT_PRE_SUNSET:
                            return 1 - timeManager.getTPeriod();

                        case TWILIGHT_POST_SUNRISE:
                            return timeManager.getTPeriod();

                        default:
                            return 0;
                    }
                }
            };

    public static TimeFuzzyPeriodMembershipFunction isNight =
            new TimeFuzzyPeriodMembershipFunction() {

                @Override
                public float evaluate(TimeManager timeManager) {
                    switch (timeManager.getPeriod()) {
                        case PRE_MIDNIGHT:
                        case POST_MIDNIGHT:
                            return 1;

                        case TWILIGHT_POST_SUNSET:
                            return timeManager.getTPeriod();

                        case TWILIGHT_PRE_SUNRISE:
                            return 1 - timeManager.getTPeriod();

                        default:
                            return 0;
                    }
                }
            };

    public static TimeFuzzyPeriodMembershipFunction isSunrise =
            new TimeFuzzyPeriodMembershipFunction() {

                @Override
                public float evaluate(TimeManager timeManager) {
                    switch (timeManager.getPeriod()) {
                        case TWILIGHT_PRE_SUNRISE:
                            return timeManager.getTPeriod();

                        case TWILIGHT_POST_SUNRISE:
                            return 1 - timeManager.getTPeriod();

                        default:
                            return 0;
                    }
                }
            };

    public static TimeFuzzyPeriodMembershipFunction isSunset =
            new TimeFuzzyPeriodMembershipFunction() {

                @Override
                public float evaluate(TimeManager timeManager) {
                    switch (timeManager.getPeriod()) {
                        case TWILIGHT_PRE_SUNSET:
                            return timeManager.getTPeriod();

                        case TWILIGHT_POST_SUNSET:
                            return 1 - timeManager.getTPeriod();

                        default:
                            return 0;
                    }
                }
            };
}
