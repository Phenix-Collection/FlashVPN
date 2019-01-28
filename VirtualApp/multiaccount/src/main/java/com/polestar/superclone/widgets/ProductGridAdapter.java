package com.polestar.superclone.widgets;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polestar.superclone.R;

import java.util.List;

/**
 * Created by Huan on 2019/1/28.
 */

public class ProductGridAdapter extends BaseAdapter{

    private List<ProductGridItem> products;
    private Context mContext;

    public ProductGridAdapter(Context context, List<ProductGridItem> list) {
        super();
        mContext = context;
        products = list;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object getItem(int position) {
        if ( products == null) {
            return  null;
        }
        if (position < products.size() && position >= 0) {
            return  products.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = new ProductGridCell(mContext);
        ImageView icon = (ImageView) view.findViewById(R.id.product_icon);
        TextView name = (TextView) view.findViewById(R.id.product_name);

        ProductGridItem productItem = (ProductGridItem) getItem(i);
        if (productItem != null) {
            if (productItem.icon == null) {
                productItem.loadIcon(mContext);
            }
            icon.setImageDrawable(productItem.icon);
            name.setText(productItem.name);
        }
        view.setTag(productItem);
        return view;
    }
}
