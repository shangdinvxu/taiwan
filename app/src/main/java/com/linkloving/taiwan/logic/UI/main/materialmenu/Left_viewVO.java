package com.linkloving.taiwan.logic.UI.main.materialmenu;

import com.linkloving.taiwan.R;

/**
 * Created by zkx on 2016/3/10.
 */
public class Left_viewVO {
//    public static final int FRIENDS = 0;
//    public static final int RANKING = FRIENDS + 1;
    public static final int Heartrate =  0;
    public static final int GOAL =  1;
    public static final int KEFU = 2;
    public static final int CLOCK =3;
    public static final int MORE = 4;



    public static int[] menuID = {
            1,2,4,5,6
    };

    public static int[] menuIDwithHR = {
        0,1,2,4,5,6
    };
    public static int[] menuIconwithHR = {
//            R.mipmap.ic_menu_friends,
//            R.mipmap.ic_menu_rank,
            R.mipmap.ic_menu_heartrate ,
            R.mipmap.ic_menu_goal,
//            R.mipmap.ic_menu_kefu,
            R.mipmap.ic_menu_device,
            R.mipmap.heartrate_menu,
            R.mipmap.icon_menu_longsit,
            R.mipmap.ic_menu_more
    };
    public static int[] menuTextwithHR = {
//            R.string.relationship,
//            R.string.ranking_title,
            R.string.menu_heartrate,
            R.string.menu_goal,
//            R.string.service_center_title,
            R.string.alarm_notify,
            R.string.menu_heartrate,
            R.string.long_sit_text,
            R.string.general_more
    };

    public static int[] menuIcon = {
//            R.mipmap.ic_menu_friends,
//            R.mipmap.ic_menu_rank,
            R.mipmap.ic_menu_goal,
//            R.mipmap.ic_menu_kefu,
            R.mipmap.ic_menu_device,
            R.mipmap.heartrate_menu,
            R.mipmap.icon_menu_longsit,
            R.mipmap.ic_menu_more
    };
    public static int[] menuText = {
//            R.string.relationship,
//            R.string.ranking_title,
            R.string.menu_goal,
//            R.string.service_center_title,
            R.string.alarm_notify,
            R.string.menu_heartrate,
            R.string.long_sit_text,
            R.string.general_more
    };
}
