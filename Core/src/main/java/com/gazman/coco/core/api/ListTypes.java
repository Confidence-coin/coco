package com.gazman.coco.core.api;

/**
 * Created by Ilya Gazman on 2/11/2018.
 */
public final class ListTypes {

    public static final int TYPE_1_TO_1 = 1;
    public static final int TYPE_1_to_MANY_SAME_AMOUNT = 2;
    public static final int TYPE_1_to_MANY_DIFFERENT_AMOUNT = 3;
    public static final int TYPE_MANY_TO_1_SAME_AMOUNT = 4;
    public static final int TYPE_MANY_TO_1_DIFFERENT_AMOUNT = 5;
    public static final int TYPE_TRUSTED_PARTY_BALANCE_UPDATE = 6;
    public static final int TYPE_1_TO_1_TRUSTS = 7;
    public static final int TYPE_1_TO_MANY_TRUSTS_SAME_FEATURES = 8;
    public static final int TYPE_1_TO_MANY_TRUSTS_DIFFERENT_FEATURES = 9;
    public static final int TYPE_WALLETS = 10;

    public static final byte FEATURE_SMBI_AMOUNT = 1;
    public static final byte FEATURE_ALL_AMOUNT = 2;
    public static final byte FEATURE_DEFAULT_FEES = 4;
    public static final byte FEATURE_TRUSTED_SIGNATURE = 8;

    public static boolean isFeatureOn(byte mask, byte feature) {
        return (mask & feature) > 0;
    }
}
