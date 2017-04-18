package com.linkloving.taiwan.utils;

import java.math.BigDecimal;

/**
 * Created by Daniel.Xu on 2017/4/6.
 */

public class MathUtils {
    public static double doubleWithTwo(double f) {
        BigDecimal b = new BigDecimal(f);
        double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1 ;
    }
}
