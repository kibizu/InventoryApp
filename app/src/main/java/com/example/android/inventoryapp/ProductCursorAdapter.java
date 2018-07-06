package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;



import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

public class    ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c)  {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        TextView nameTextView = view.findViewById(R.id.product_name);
        TextView quantityView = view.findViewById(R.id.product_quantity);
        TextView priceView = view.findViewById(R.id.product_price);
        final Button button = view.findViewById(R.id.sale_button);

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

        String productName = cursor.getString(nameColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);
        double productPrice = cursor.getDouble(priceColumnIndex);

        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        final String productId= cursor.getString(idColumnIndex);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                CatalogActivity catalogActivity = (CatalogActivity) context;
                catalogActivity.decreaseQuantity( Integer.valueOf(productId), Integer.valueOf(productQuantity));

            }
        });

        nameTextView.setText(productName);
        quantityView.setText(context.getResources().getString(R.string.hint_product_quantity) + ": " + String.valueOf(productQuantity));
        priceView.setText(context.getResources().getString(R.string.hint_product_price) + ": " + String.valueOf(productPrice));

    }
}
