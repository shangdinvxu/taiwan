package com.linkloving.taiwan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;

import Trace.GreenDao.DaoMaster;
import Trace.GreenDao.NoteDao;
import Trace.GreenDao.heartrateDao;

/**
 * Created by Daniel.Xu on 2017/4/27.
 */

public class MySQLiteOpenHelper extends  DaoMaster.OpenHelper {
    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db,heartrateDao.class,NoteDao.class);
    }
}
