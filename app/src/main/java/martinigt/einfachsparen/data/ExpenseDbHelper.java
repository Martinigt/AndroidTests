package martinigt.einfachsparen.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import martinigt.einfachsparen.model.Expense;

/**
 * Created by martin on 16.04.16.
 */
public class ExpenseDbHelper {

    public static final String EXPENSE_TABLE_NAME = "Expense";

    public static final String EXPENSE_ID = "_id";
    public static final String EXPENSE_PERIOD_ID = "periodId";
    public static final String EXPENSE_IS_STANDARD = "standard";
    public static final String EXPENSE_NAME = "name";
    public static final String EXPENSE_VALUE = "value";
    public static final String EXPENSE_DATE = "date";
    public static final String EXPENSE_TAG = "tag";

    public static final String EXPENSE_SCHEMA_CREATE = "CREATE TABLE " + EXPENSE_TABLE_NAME + " (" + EXPENSE_ID + " INTEGER " +
            "PRIMARY KEY, " + EXPENSE_PERIOD_ID + " INTEGER, " + EXPENSE_IS_STANDARD +" INTEGER, " +
            EXPENSE_NAME + " TEXT, " + EXPENSE_VALUE + " REAL, " + EXPENSE_TAG + " TEXT, " +
            EXPENSE_DATE + " INTEGER)";

    public static final String EXPENSE_DROP_TABLE = "DROP TABLE IF EXISTS " + EXPENSE_TABLE_NAME;

    public static final String EXPENSE_GET_ALL_DEFAULT = "SELECT * FROM " + EXPENSE_TABLE_NAME + " WHERE " +
            EXPENSE_IS_STANDARD + " >= 1";

    private static final String EXPENSE_DELETE_TABLE = "DELETE FROM " + EXPENSE_TABLE_NAME;

    private DatabaseHelper dbHelper;

    public ExpenseDbHelper(DatabaseHelper helper) {
        dbHelper = helper;
    }

    public void cleanTable() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(EXPENSE_DELETE_TABLE);
    }

    public ArrayList<Expense> getAllDefaultExpenses() {
        ArrayList<Expense> result = new ArrayList<Expense>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor allResults = db.rawQuery(EXPENSE_GET_ALL_DEFAULT, null);
        while (allResults.moveToNext()) {
            result.add(getExpenseFromCursor(allResults));
        }
        return result;
    }

    public ArrayList<Expense> getAllExpensesForPeriod(int periodId) {
        return getAllExpensesForPeriod(periodId, false);
    }

    public ArrayList<Expense> getAllExpensesForPeriod(int periodId, boolean mostRecentFirst) {
        ArrayList<Expense> result = new ArrayList<Expense>();
        SQLiteDatabase db =  dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " +  EXPENSE_TABLE_NAME + " WHERE " +
                periodId + " = " + EXPENSE_PERIOD_ID;
        if (mostRecentFirst) {
            query = query + " ORDER BY " + EXPENSE_ID + " DESC";
        }
        Cursor dbResults = db.rawQuery(query, null);
        while (dbResults.moveToNext()) {
            result.add(getExpenseFromCursor(dbResults, periodId));
        }
        return result;
    }

    private Expense getExpenseFromCursor(Cursor cursor, int periodeId){
        Expense result = new Expense(periodeId);
        result.setId(cursor.getInt(cursor.getColumnIndex(EXPENSE_ID)));
        result.setStandard(cursor.getInt(cursor.getColumnIndex(EXPENSE_IS_STANDARD)) > 0);
        result.setName(cursor.getString(cursor.getColumnIndex(EXPENSE_NAME)));
        result.setValue(cursor.getDouble(cursor.getColumnIndex(EXPENSE_VALUE)));
        result.setDate(new Date(cursor.getLong(cursor.getColumnIndex(EXPENSE_DATE))));
        result.setTag(cursor.getString(cursor.getColumnIndex(EXPENSE_TAG)));
        return result;
    }

    private Expense getExpenseFromCursor(Cursor cursor){
        return this.getExpenseFromCursor(cursor, -1);
    }

    public void storeListOfExpenses(ArrayList<Expense> expenses) {
        for (Expense currentExpense : expenses
                ) {
            addExpense(currentExpense);
        }
    }

    public boolean addExpense(Expense expenseToAdd) {
        boolean result = false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EXPENSE_NAME, expenseToAdd.getName());
        contentValues.put(EXPENSE_TAG, expenseToAdd.getTag());
        contentValues.put(EXPENSE_VALUE, expenseToAdd.getValue());
        contentValues.put(EXPENSE_DATE, expenseToAdd.getDate().getTime());
        contentValues.put(EXPENSE_IS_STANDARD, expenseToAdd.isStandard());
        contentValues.put(EXPENSE_PERIOD_ID, expenseToAdd.getPeriodId());
        try {
            db.insert(EXPENSE_TABLE_NAME, null, contentValues);
            result = true;
        }
        catch (Exception e) {
            result = false;
        }
        return result;
    }

    public boolean deleteExpense(Expense expenseToDelete) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(EXPENSE_TABLE_NAME, EXPENSE_ID + " = " + expenseToDelete.getId(), null) > 0;
    }

    public boolean updateExpense(Expense expenseToUpdate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(EXPENSE_NAME, expenseToUpdate.getName());
        contentValues.put(EXPENSE_TAG, expenseToUpdate.getTag());
        contentValues.put(EXPENSE_VALUE, expenseToUpdate.getValue());
        contentValues.put(EXPENSE_IS_STANDARD, expenseToUpdate.isStandard());
        return db.update(EXPENSE_TABLE_NAME, contentValues, EXPENSE_ID + " = " + expenseToUpdate.getId(), null) > 0;
    }
}
