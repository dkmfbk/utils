package eu.fbk.utils.eval;

import java.text.NumberFormat;

/**
 * Utility class for aggregating counts of true positives, false positives, and
 * false negatives and computing precision/recall/F1 stats. Can be used for a single
 * collection of stats, or to aggregate stats from a bunch of runs.
 *
 * @author Joseph Smarr
 */

public class PrecisionRecallStats {

    /**
     * Count of true positives.
     */
    protected int tpCount = 0;

    /**
     * Count of false positives.
     */
    protected int fpCount = 0;

    /**
     * Count of false negatives.
     */
    protected int fnCount = 0;

    /**
     * Constructs a new PrecisionRecallStats with initially 0 counts.
     */
    public PrecisionRecallStats() {
        this(0, 0, 0);
    }

    /**
     * Constructs a new PrecisionRecallStats with the given initial counts.
     */
    public PrecisionRecallStats(int tp, int fp, int fn) {
        tpCount = tp;
        fpCount = fp;
        fnCount = fn;
    }

    /**
     * Returns the current count of true positives.
     */
    public int getTP() {
        return tpCount;
    }

    /**
     * Returns the current count of false positives.
     */
    public int getFP() {
        return fpCount;
    }

    /**
     * Returns the current count of false negatives.
     */
    public int getFN() {
        return fnCount;
    }

    /**
     * Adds the given number to the count of true positives.
     */
    public void addTP(int count) {
        tpCount += count;
    }

    /**
     * Adds one to the count of true positives.
     */
    public void incrementTP() {
        addTP(1);
    }

    /**
     * Adds the given number to the count of false positives.
     */
    public void addFP(int count) {
        fpCount += count;
    }

    /**
     * Adds one to the count of false positives.
     */
    public void incrementFP() {
        addFP(1);
    }

    /**
     * Adds the given number to the count of false negatives.
     */
    public void addFN(int count) {
        fnCount += count;
    }

    /**
     * Adds one to the count of false negatives.
     */
    public void incrementFN() {
        addFN(1);
    }

    /**
     * Adds the counts from the given stats to the counts of this stats.
     */
    public void addCounts(PrecisionRecallStats prs) {
        addTP(prs.getTP());
        addFP(prs.getFP());
        addFN(prs.getFN());
    }

    /**
     * Returns the current precision: tp/(tp+fp).
     * Returns 1.0 if tp and fp are both 0.
     */
    public double getPrecision() {
        if (tpCount == 0 && fpCount == 0) {
            return 1.0;
        }
        return ((double) tpCount) / (tpCount + fpCount);
    }

    /**
     * Returns a String summarizing precision that will print nicely.
     */
    public String getPrecisionDescription(int numDigits) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(numDigits);
        return nf.format(getPrecision()) + "  (" + tpCount + "/" + (tpCount + fpCount) + ")";
    }

    /**
     * Returns the current recall: tp/(tp+fn).
     * Returns 1.0 if tp and fn are both 0.
     */
    public double getRecall() {
        if (tpCount == 0 && fnCount == 0) {
            return 1.0;
        }
        return ((double) tpCount) / (tpCount + fnCount);
    }

    /**
     * Returns a String summarizing recall that will print nicely.
     */
    public String getRecallDescription(int numDigits) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(numDigits);
        return nf.format(getRecall()) + "  (" + tpCount + "/" + (tpCount + fnCount) + ")";
    }

    /**
     * Returns the current F1 measure (alpha=0.5).
     */
    public double getFMeasure() {
        return getFMeasure(0.5);
    }

    /**
     * Returns the F-Measure with the given mixing parameter (must be between 0 and 1).
     * If either precision or recall are 0, return 0.0.
     * F(alpha) = 1/(alpha/precision + (1-alpha)/recall)
     */
    public double getFMeasure(double alpha) {
        double pr = getPrecision();
        double re = getRecall();
        if (pr == 0 || re == 0) {
            return 0.0;
        }
        return 1.0 / ((alpha / pr) + (1.0 - alpha) / re);
    }

    /**
     * Returns a String summarizing F1 that will print nicely.
     */
    public String getF1Description(int numDigits) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(numDigits);
        return nf.format(getFMeasure());
    }

    /**
     * Returns a String representation of this PrecisionRecallStats, indicating the number of tp, fp, fn counts.
     */
    @Override
    public String toString() {
        return "PrecisionRecallStats[tp=" + getTP() + ",fp=" + getFP() + ",fn=" + getFN() + "]";
    }

    public String toString(int numDigits) {
        return "PrecisionRecallStats[tp=" + getTP() + ",fp=" + getFP() + ",fn=" + getFN() +
                ",p=" + getPrecisionDescription(numDigits) + ",r=" + getRecallDescription(numDigits) +
                ",f1=" + getF1Description(numDigits) + "]";
    }

}
