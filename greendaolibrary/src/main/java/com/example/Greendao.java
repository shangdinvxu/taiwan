package com.example;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class Greendao {
    public static void main(String[] args)throws  Exception{
        // 两个参数分别代表：数据库版本号与自动生成代码的包路径。
        Schema schema = new Schema(11, "Trace.GreenDao");
        // 模式（Schema）同时也拥有两个默认的 flags，分别用来标示 entity 是否是 activie 以及是否使用 keep sections。
        schema.enableActiveEntitiesByDefault();
        schema.enableKeepSectionsByDefault();
        // 一旦你拥有了一个 Schema 对象后，你便可以使用它添加实体（Entities）了。
        addNote(schema) ;

        // 最后我们将使用 DAOGenerator 类的 generateAll() 方法自动生成代码，此处你需要根据自己的情况
        // 更改输出目录（既之前创建的 java-gen)。
        new DaoGenerator().generateAll(schema, "/Danielproject/taiwan/app/src/main/java-gen");
    }

    public static void addNote(Schema schema){
        // 一个实体（类）就关联到数据库中的一张表，此处表名为「Note」（既类名）
        Entity note = schema.addEntity("Note");
        note.addIdProperty().autoincrement();
//        每天的日期
        note.addStringProperty("date");
//        轨迹开始的时间
        note.addDateProperty("startDate");
//        轨迹进行时的时间
        note.addDateProperty("runDate");
//        数据类型
        note.addIntProperty("type");
//        轨迹的纬度
        note.addDoubleProperty("latitude");
//        经度
        note.addDoubleProperty("longitude");

        Entity heartrate = schema.addEntity("heartrate");
        heartrate.addIdProperty().autoincrement();
        heartrate.addIntProperty("startTime").unique();
        heartrate.addIntProperty("max");
        heartrate.addIntProperty("avg");

    }
}
