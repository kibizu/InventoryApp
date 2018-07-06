package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.inventoryapp.data.ProductContract.ProductEntry;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mProductNameEditText;
    private EditText mProductPriceEditText;
    private EditText mProductAuthorEditText;
    private EditText mProductSupplierNameEditText;
    private EditText mProductSupplierPhoneNumberEditText;
    private TextView mProductQuantityTextView;
    private Button mButtonDash;
    private Button mButtonPlus;
    private Button mButtonDelete;
    private Button mButtonOrder;

    private int quantityAddProduct;

    private static final int PRODUCT_LOADER = 0;

    private Uri currentProductUri;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mProductNameEditText = findViewById(R.id.edit_product_name);
        mProductPriceEditText = findViewById(R.id.edit_product_price);
        mProductAuthorEditText = findViewById(R.id.edit_product_author);
        mProductSupplierNameEditText = findViewById(R.id.edit_product_supplier_name);
        mProductSupplierPhoneNumberEditText = findViewById(R.id.edit_product_supplier_phone_number);
        mProductQuantityTextView = findViewById(R.id.edit_product_quantity);
        mButtonDash = findViewById(R.id.dash);
        mButtonPlus = findViewById(R.id.plus);
        mButtonDelete = findViewById(R.id.delete);
        mButtonOrder = findViewById(R.id.order);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if(currentProductUri==null){
            mButtonDelete.setVisibility(View.GONE);
            mButtonOrder.setVisibility(View.GONE);
            setTitle(R.string.editor_activity_title_new_product);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();

            quantityAddProduct = 0;

            mButtonDash.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (quantityAddProduct > 0){

                        quantityAddProduct = quantityAddProduct - 1;
                        mProductQuantityTextView.setText(String.valueOf(quantityAddProduct));

                    }else{

                        Toast.makeText(getBaseContext(), getString(R.string.quantity_cannot_be_negative), Toast.LENGTH_SHORT).show();

                    }

                }
            });

            mButtonPlus.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    quantityAddProduct = quantityAddProduct + 1;
                    mProductQuantityTextView.setText(String.valueOf(quantityAddProduct));

                }
            });

        } else {

            setTitle(R.string.editor_activity_title_edit_product);
            getLoaderManager().initLoader(PRODUCT_LOADER,null,this);

        }

        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mProductAuthorEditText.setOnTouchListener(mTouchListener);
        mProductSupplierNameEditText.setOnTouchListener(mTouchListener);
        mProductSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);
        mButtonDash.setOnTouchListener(mTouchListener);
        mButtonPlus.setOnTouchListener(mTouchListener);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (currentProductUri == null) {

            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);

        }

        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct(){

        if (currentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the currentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();

    }

    private boolean saveProduct(){

        Uri uri;

        ContentValues values = new ContentValues();

        String productNameString = mProductNameEditText.getText().toString().trim();
        String productPriceString = mProductPriceEditText.getText().toString();
        String productAuthorString = mProductAuthorEditText.getText().toString().trim();
        String supplierNameString = mProductSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mProductSupplierPhoneNumberEditText.getText().toString().trim();
        String productQuantityString = mProductQuantityTextView.getText().toString().trim();

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.

        if (currentProductUri == null && TextUtils.isEmpty(productNameString) && TextUtils.isEmpty(productPriceString) && TextUtils.isEmpty(productQuantityString)
                && TextUtils.isEmpty(supplierPhoneNumberString)) {

            return false;

        }

        if (TextUtils.isEmpty(productNameString)){

            Toast.makeText(this, getString(R.string.product_name_required), Toast.LENGTH_SHORT).show();
            return false;

        }

        double productPrice = 0.00;

        if (!TextUtils.isEmpty(productPriceString)) {

            productPrice = Double.parseDouble(productPriceString);

        } else {

            Toast.makeText(this, getString(R.string.product_valid_price), Toast.LENGTH_SHORT).show();
            return false;

        }

        if (TextUtils.isEmpty(supplierNameString)){

            Toast.makeText(this, getString(R.string.product_supplier_name_required), Toast.LENGTH_SHORT).show();
            return false;

        }

        int supplierPhoneNumber = 0;

        if (!TextUtils.isEmpty(supplierPhoneNumberString)) {

            supplierPhoneNumber = Integer.parseInt(supplierPhoneNumberString);

        } else {

            Toast.makeText(this, getString(R.string.product_valid_supplier_phone_number), Toast.LENGTH_SHORT).show();
            return false;

        }

        int productQuantity = 0;

        if(!TextUtils.isEmpty(productQuantityString)) {

            productQuantity = Integer.parseInt(productQuantityString);

        }

        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
        values.put(ProductEntry.COLUMN_PRODUCT_AUTHOR, productAuthorString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, supplierPhoneNumber);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);

        if(currentProductUri == null) {

            uri = getContentResolver().insert(ProductEntry.CONTENT_URI,values);
            if (uri==null) {
                // If the row ID is -1, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed), Toast.LENGTH_SHORT).show();
                return true;
            }else{

                Toast.makeText(this, getString(R.string.editor_insert_product_successful), Toast.LENGTH_SHORT).show();
                return true;
            }

        } else {

            int rowsUpdated = getContentResolver().update(currentProductUri,values,null,null);

            if (rowsUpdated==0){

                Toast.makeText(this, getString(R.string.editor_update_product_failed), Toast.LENGTH_SHORT).show();
                return true;

            }else {

                Toast.makeText(this, getString(R.string.editor_update_product_succesful), Toast.LENGTH_SHORT).show();
                return true;

            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                if (saveProduct()) {
                    //Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:

                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

                if(!mProductHasChanged){

                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;

                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                BaseColumns._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_AUTHOR,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER
        };

        return new CursorLoader(
                this,
                currentProductUri,
                projection,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if(cursor.moveToFirst()){

            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
            int authorColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_AUTHOR);

            mProductNameEditText.setText(cursor.getString(nameColumnIndex));
            mProductQuantityTextView.setText(cursor.getString(quantityColumnIndex));
            mProductPriceEditText.setText(String.valueOf(cursor.getDouble(priceColumnIndex)));
            mProductSupplierNameEditText.setText(cursor.getString(supplierNameColumnIndex));
            mProductSupplierPhoneNumberEditText.setText(cursor.getString(supplierPhoneNumberColumnIndex));
            mProductAuthorEditText.setText(cursor.getString(authorColumnIndex));

            quantityAddProduct = cursor.getInt(quantityColumnIndex);

            mButtonDash.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (quantityAddProduct > 0){

                        quantityAddProduct = quantityAddProduct - 1;
                        mProductQuantityTextView.setText(String.valueOf(quantityAddProduct));

                    }else{

                        Toast.makeText(getBaseContext(), getString(R.string.quantity_cannot_be_negative), Toast.LENGTH_SHORT).show();

                    }

                }
            });

            mButtonPlus.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    quantityAddProduct = quantityAddProduct + 1;
                    mProductQuantityTextView.setText(String.valueOf(quantityAddProduct));

                }
            });

            mButtonDelete.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){

                    showDeleteConfirmationDialog();

                }

            });

            final String supplierPhoneNumber = cursor.getString(supplierPhoneNumberColumnIndex);

            mButtonOrder.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + supplierPhoneNumber));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }

                }

            });

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mProductNameEditText.setText("");
        mProductQuantityTextView.setText("0");
        mProductPriceEditText.setText("");
        mProductSupplierNameEditText.setText("");
        mProductSupplierPhoneNumberEditText.setText("");
        mProductAuthorEditText.setText("");

    }
}
