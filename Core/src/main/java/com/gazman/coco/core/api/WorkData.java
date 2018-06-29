package com.gazman.coco.core.api;

import java.util.Arrays;

/**
 * Created by Ilya Gazman on 1/18/2018.
 */
public class WorkData {
    public int workId;
    public byte[] step3Hash;
    public byte[] difficulty;
    public String shareErrorMessage;

    @Override
    public int hashCode() {
        return Integer.hashCode(workId);
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof WorkData)) {
            return false;
        }
        WorkData workData = (WorkData) obj;
        return workData.workId == workId;
    }

    @Override
    public String toString() {
        return "WorkData{" +
                "workId=" + workId +
                ", step3Hash=" + Arrays.toString(step3Hash) +
                ", difficulty=" + Arrays.toString(difficulty) +
                ", shareErrorMessage='" + shareErrorMessage + '\'' +
                '}';
    }
}
