package martinigt.einfachsparen.forms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import martinigt.einfachsparen.R;
import martinigt.einfachsparen.data.DatabaseHelper;
import martinigt.einfachsparen.data.ExpenseDbHelper;
import martinigt.einfachsparen.data.IncomeDbHelper;
import martinigt.einfachsparen.helper.Helper;
import martinigt.einfachsparen.model.Expense;
import martinigt.einfachsparen.model.Income;
import martinigt.einfachsparen.model.Transaction;

public class EditTransactionActivity extends AppCompatActivity implements TextWatcher, DialogInterface.OnClickListener {

    private EditText transactionNameInput;

    private EditText transactionValueInput;

    private CheckBox transactionRecurringInput;

    private MenuItem saveMenuItem;

    private Transaction transactionToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getReferencesToWidgets();

        Intent sourceIntent = getIntent();
        transactionToEdit = (Transaction) sourceIntent.getSerializableExtra("TransactionToEdit");

        readValuesFromTransaction();

        bindListeners();
    }

    private void bindListeners() {
        transactionNameInput.addTextChangedListener(this);
        transactionValueInput.addTextChangedListener(this);
    }

    private void getReferencesToWidgets() {
        transactionNameInput = (EditText) findViewById(R.id.editTransactionNameInput);
        transactionValueInput = (EditText) findViewById(R.id.editTransactionValueInput);
        transactionRecurringInput = (CheckBox) findViewById(R.id.editTransactionRecurringInput);
    }

    private void readValuesFromTransaction() {
        transactionNameInput.setText(transactionToEdit.getName());
        transactionValueInput.setText(""+ transactionToEdit.getValue());
        transactionRecurringInput.setChecked(transactionToEdit.isStandard());
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_saveTransaction:
                if (validateInput()) {
                    updateTransaction();
                }
                return true;
            case R.id.action_deleteTransaction:
                askDeletionConfirmation();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        boolean validationResult = validateInput();
        saveMenuItem.setEnabled(validationResult);
        saveMenuItem.setVisible(validationResult);
    }

    public void afterTextChanged(Editable editable) {

    }

    private boolean validateInput() {
        boolean result = true;
        result &= Helper.validateMandatoryTextField(transactionNameInput);
        result &= Helper.validatePositiveDoubleField(transactionValueInput);
        return result;
    }

    private void askDeletionConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirmQuestion).setPositiveButton(R.string.confirmYes, this)
                .setNegativeButton(R.string.confirmNo, this).show();
    }

    private void deleteTransaction() {
        boolean result = true;
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        if (transactionToEdit instanceof Income) {
            IncomeDbHelper incomeDbHelper = new IncomeDbHelper(dbHelper);
            result = incomeDbHelper.deleteIncome((Income) transactionToEdit);
        }
        else {
            ExpenseDbHelper expenseDbHelper = new ExpenseDbHelper(dbHelper);
            result = expenseDbHelper.deleteExpense((Expense) transactionToEdit);
        }
        if (result) {
            finish();
        }
    }

    private void updateTransaction() {
        boolean result = true;
        transactionToEdit.setName(transactionNameInput.getText().toString());
        transactionToEdit.setValue(Double.parseDouble(transactionValueInput.getText().toString()));
        transactionToEdit.setStandard(transactionRecurringInput.isChecked());
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        if (transactionToEdit instanceof Income) {
            IncomeDbHelper incomeDbHelper = new IncomeDbHelper(dbHelper);
            result = incomeDbHelper.updateIncome((Income) transactionToEdit);
        }
        else {
            ExpenseDbHelper expenseDbHelper = new ExpenseDbHelper(dbHelper);
            result = expenseDbHelper.updateExpense((Expense) transactionToEdit);
        }
        if (result) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_transaction, menu);
        saveMenuItem = menu.findItem(R.id.action_saveTransaction);
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                deleteTransaction();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    }

}
