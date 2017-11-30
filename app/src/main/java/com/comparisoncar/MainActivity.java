package com.comparisoncar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.comparisoncar.bean.CarComparisonBean;
import com.comparisoncar.bean.ComparisonCarItem;
import com.comparisoncar.vhtableview.VHBaseAdapter;
import com.comparisoncar.vhtableview.VHTableView;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String IDS = "ids";

    public String CC = "●标配 ○选配 -无";

    private VHTableView vht_table;
    private ImageView mImgLeft;
    private ImageView mImgRight;

    //可以得到屏幕的宽度  按 屏幕的比例去划分  后续修改
    private int title0Width;
    private int titleWidth;
    private int titleHieght;
    private int cellHieght;
    private int titleLeftPadding;

    ArrayList<ArrayList<ComparisonCarItem>> contentAllData;//显示数据
    ArrayList<ArrayList<ComparisonCarItem>> contentData;//隐藏相同项时  零时保存数据
    private ArrayList<ComparisonCarItem> titleData;
    private boolean isComparsionSame;//对比中
    private String ids;
    private int mScrollX;
    private Button mBtnJump;


    public static void gotoHere(Activity act, int size) {
        Intent intent = new Intent(act, MainActivity.class);
        intent.putExtra("size", size);
        act.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();

        final Random random = new Random();
        final int i = random.nextInt(contentAllData.size());
        mBtnJump.setText("随机跳转到第" + i);
        mBtnJump.setTag(i);
        mBtnJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vht_table.setSelection((Integer) mBtnJump.getTag(), ((int) getResources().getDimension(R.dimen.dimen_title_height)));
                int i = random.nextInt(contentAllData.size());
                mBtnJump.setText("随机跳转到第" + i);
                mBtnJump.setTag(i);
            }
        });
    }

    protected void initView() {
        ids = getIntent().getStringExtra(IDS);
        vht_table = (VHTableView) findViewById(R.id.vht_table);
        mImgLeft = (ImageView) findViewById(R.id.img_left);
        mImgRight = (ImageView) findViewById(R.id.img_right);
        mBtnJump = (Button) findViewById(R.id.btn_jump);
        mImgLeft.setVisibility(View.GONE);
        mImgRight.setVisibility(View.GONE);
        vht_table.setOnUIScrollChanged(new VHTableView.OnUIScrollChanged() {
            @Override
            public void onUIScrollChanged(int l, int oldl, int maxScrollX, int getScrollX) {
                mScrollX = l;
                mImgRight.setVisibility(View.VISIBLE);
                mImgLeft.setVisibility(View.VISIBLE);
                //滑到最左
                if (getScrollX < 3) {
                    mImgLeft.setVisibility(View.GONE);
                } else if (getScrollX + 3 >= maxScrollX) {  //滑到最右
                    mImgRight.setVisibility(View.GONE);
                } else {  //滑到中间
                }
            }
        });
        mImgRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollLastPoint(mScrollX + titleWidth);
            }
        });
        mImgLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollLastPoint(mScrollX - titleWidth);
            }
        });
    }


    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    protected void initData() {
        if (isComparsionSame && !contentAllData.isEmpty()) {
            toggleData();
        }
        Gson gson = new Gson();
        CarComparisonBean carComparisonBean = gson.fromJson(json, CarComparisonBean.class);
        {
            try {
                vht_table.setVisibility(View.VISIBLE);
                List<CarComparisonBean.ParamEntity> data = carComparisonBean.getParam();

                //设置数据源
                titleData = new ArrayList<>();

                //title 第一个空格
                ComparisonCarItem comparisonCarItem = new ComparisonCarItem();
                comparisonCarItem.setName("");
                titleData.add(comparisonCarItem);

                //title 第一行车型名称
                List<CarComparisonBean.ParamEntity.ParamitemsEntity.ValueitemsEntity> paramitems = data.get(0).getParamitems().get(0).getValueitems();
                for (CarComparisonBean.ParamEntity.ParamitemsEntity.ValueitemsEntity paramitem : paramitems) {

                    ComparisonCarItem comparisonCarItem2 = new ComparisonCarItem();
                    comparisonCarItem2.setName(paramitem.getValue());
                    comparisonCarItem2.setId(paramitem.getSpecid());
                    titleData.add(comparisonCarItem2);
                }
//title 最后的+号
                ComparisonCarItem comparisonCarItem3 = new ComparisonCarItem();
                comparisonCarItem3.setImgBackgroud(R.drawable.icon_tianjia);
                titleData.add(comparisonCarItem3);
                //
                data.get(0).getParamitems().remove(0);


                //一大坨数据处理  没有写好    车的具体参数
                contentAllData = new ArrayList<>();
                for (CarComparisonBean.ParamEntity paramEntity : data) {

                    for (CarComparisonBean.ParamEntity.ParamitemsEntity paramitemsEntity : paramEntity.getParamitems()) {
                        ArrayList<ComparisonCarItem> contentRowData = new ArrayList<>();
                        //每一行的第一个
                        ComparisonCarItem comparisonCarItem4 = new ComparisonCarItem();
                        if (paramitemsEntity.getName().indexOf("(") <= 5) {
                            comparisonCarItem4.setName(paramitemsEntity.getName().replace("(", "\n("));
                        } else {
                            comparisonCarItem4.setName(paramitemsEntity.getName());
                        }
                        if (!contentAllData.isEmpty()) {//判断行标题
                            ComparisonCarItem lastComparisonCar = contentAllData.get(contentAllData.size() - 1).get(0);
                            comparisonCarItem4.setHeader(!lastComparisonCar.getRowTitle().equals(paramEntity.getName()));
                        }
                        comparisonCarItem4.setRowTitle(paramEntity.getName());
                        contentRowData.add(comparisonCarItem4);
                        //每一行中的具体车型
                        //开始判断这一行的值是否全部相同
                        boolean valueSame = true;
                        String value = null;
                        for (CarComparisonBean.ParamEntity.ParamitemsEntity.ValueitemsEntity valueitemsEntity : paramitemsEntity.getValueitems()) {
                            ComparisonCarItem comparisonCarItem5 = new ComparisonCarItem();
                            comparisonCarItem5.setName(valueitemsEntity.getValue());
                            comparisonCarItem5.setId(valueitemsEntity.getSpecid());
//                            comparisonCarItem5.setColor(valueitemsEntity.getColor());
                            contentRowData.add(comparisonCarItem5);
                            if (valueSame) {
                                if (!TextUtils.isEmpty(value)) {
                                    valueSame = value.equals(comparisonCarItem5.getName());
                                }
                                value = comparisonCarItem5.getName();
                            }
                        }
                        comparisonCarItem4.setSame(valueSame);
                        //每一行中的最后一个 虚位
                        ComparisonCarItem comparisonCarItem6 = new ComparisonCarItem();
                        comparisonCarItem6.setName("-");
                        contentRowData.add(comparisonCarItem6);

                        contentAllData.add(contentRowData);
                    }

                }
                setAdapter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void toggleData() {
        isComparsionSame = !isComparsionSame;
        if (isComparsionSame) {
            if (contentData == null) {
                contentData = new ArrayList<>();
            }
            contentData.clear();
            try {
                contentData.addAll(deepCopy(contentAllData));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            ArrayList<ArrayList<ComparisonCarItem>> temp = new ArrayList<>();
            //一大坨数据处理  没有写好
            String rowTitle = null;
            for (ArrayList<ComparisonCarItem> comparisonCarItems : contentAllData) {
                ComparisonCarItem comparisonCarItem = comparisonCarItems.get(0);
                if (comparisonCarItem.isSame()) {
                    temp.add(comparisonCarItems);
                    if (comparisonCarItem.isHeader()) {
                        rowTitle = comparisonCarItem.getRowTitle();
                    }
                } else {
                    if (!comparisonCarItem.isHeader() && comparisonCarItem.getRowTitle().equals(rowTitle)) {
                        comparisonCarItem.setHeader(true);
                        rowTitle = null;
                    }
                }
            }
            contentAllData.removeAll(temp);
        } else {
            contentAllData.clear();
            contentAllData.addAll(contentData);
        }
        setAdapter();
    }

    //使用序列化方法（相对靠谱的方法）
    public static <T> List<T> deepCopy(List<T> src) throws IOException,
            ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut
                .toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<T> dest = (List<T>) in.readObject();
        return dest;
    }

    private void setAdapter() {

        //● ○
        VHTableAdapter tableAdapter = new VHTableAdapter(MainActivity.this);
//                    vht_table.setFirstColumnIsMove(true);//设置第一列是否可移动,默认不可移动
//                    vht_table.setShowTitle(false);//设置是否显示标题行,默认显示
        //一般表格都只是展示用的，所以这里没做刷新，真要刷新数据的话，重新setadaper一次吧
        vht_table.setAdapter(tableAdapter);
        vht_table.setCurrentTouchView(vht_table.getFirstHListViewScrollView());
        if (vht_table.getTitleLayout() != null) {
            View suspensionView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_comparison_cell_header, null);
            final TextView tvSuspension = (TextView) suspensionView.findViewById(R.id.tv_title);
            final TextView tvsubTitle = (TextView) suspensionView.findViewById(R.id.tv_subtitle);
            tvsubTitle.setText(CC);

            vht_table.addTitleLayout(suspensionView);

            vht_table.getListView().setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    Log.i("w", "setOnScrollListener" + firstVisibleItem + "==" + visibleItemCount + "===" + totalItemCount);
                    if (contentAllData.size() - 1 <= firstVisibleItem)
                        return;
                    String rowTitle = contentAllData.get(firstVisibleItem).get(0).getRowTitle();
                    String nextRowTitle = contentAllData.get(firstVisibleItem + 1).get(0).getRowTitle();

                    tvSuspension.setText(rowTitle);

                    if (rowTitle == nextRowTitle) {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) vht_table.getTitleLayout().getLayoutParams();
                        params.topMargin = 0;
                        vht_table.getTitleLayout().setLayoutParams(params);
                    }

                    if (rowTitle != nextRowTitle) {
                        View childView = view.getChildAt(0);
                        if (childView != null) {
                            int titleHeight = vht_table.getTitleLayout().getHeight();
                            int bottom = childView.getBottom();
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) vht_table.getTitleLayout().getLayoutParams();
                            if (bottom < titleHeight) {
                                float pushedDistance = bottom - titleHeight;
                                params.topMargin = (int) pushedDistance;
                                vht_table.getTitleLayout().setLayoutParams(params);
                            } else {
                                if (params.topMargin != 0) {
                                    params.topMargin = 0;
                                    vht_table.getTitleLayout().setLayoutParams(params);
                                }
                            }
                        }
                    }
                }
            });
        }

        //滚动到上次位置
        scrollLastPoint(mScrollX);
    }

    private void scrollLastPoint(final int x) {
        vht_table.post(new Runnable() {
            @Override
            public void run() {
                vht_table.onUIScrollChanged(x, 0);
            }
        });
    }


    public class VHTableAdapter implements VHBaseAdapter {
        private Context context;

        public VHTableAdapter(Context context) {
            this.context = context;

            title0Width = (int) getResources().getDimensionPixelSize(R.dimen.dimen_size_60dp);
            titleWidth = (int) getResources().getDimensionPixelSize(R.dimen.dimen_size_95dp);
            titleHieght = (int) getResources().getDimensionPixelSize(R.dimen.dimen_size_75dp);
            cellHieght = (int) getResources().getDimension(R.dimen.dimen_size_50dp);
            titleLeftPadding = (int) getResources().getDimensionPixelSize(R.dimen.dimen_size_3dp);
        }

        //表格内容的行数，不包括标题行
        @Override
        public int getContentRows() {
            return contentAllData.size();
        }

        //列数
        @Override
        public int getContentColumn() {
            return titleData.size();
        }

        //标题的view，这里从0开始，这里要注意，一定要有view返回去，不能为null，每一行
        // 各列的宽度就等于标题行的列的宽度，且边框的话，自己在这里和下文的表格单元格view里面设置
        @Override
        public View getTitleView(final int columnPosition, ViewGroup parent) {
            FrameLayout view = (FrameLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_comparison_header, null);
            view.setMinimumWidth(titleHieght);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            ImageView imgDel = (ImageView) view.findViewById(R.id.img_del);
            ImageView imgAdd = (ImageView) view.findViewById(R.id.img_add);
            ImageView imgRank = (ImageView) view.findViewById(R.id.iv_item_rank_pic);
            tvTitle.setTextColor(getResources().getColor(R.color.black_font_color));
            tvTitle.setHeight(titleHieght);
            view.setBackgroundResource(R.drawable.bg_shape_gray);
            ComparisonCarItem comparisonCarItem = titleData.get(columnPosition);
            if (0 == columnPosition) {//第一列
                imgDel.setVisibility(View.GONE);
                imgRank.setVisibility(View.GONE);
                tvTitle.setWidth(title0Width);
            } else {
//                UserUtils.parseMsn(imgRank, comparisonCarItem.getRankUrl());
                imgRank.setVisibility(TextUtils.isEmpty(comparisonCarItem.getName()) ? View.GONE : View.VISIBLE);
                imgDel.setVisibility(TextUtils.isEmpty(comparisonCarItem.getName()) ? View.GONE : View.VISIBLE);
                imgDel.setOnClickListener(new View.OnClickListener() {//删除按钮
                    @Override
                    public void onClick(View v) {
                        titleData.remove(columnPosition);
                        for (ArrayList<ComparisonCarItem> comparisonCarItems : contentAllData) {
                            comparisonCarItems.remove(columnPosition);
                            //开始判断这一行的值是否全部相同
                            boolean valueSame = true;
                            String value = null;
                            for (int i = 1; i < comparisonCarItems.size() - 1; i++) {
                                if (valueSame) {
                                    if (!TextUtils.isEmpty(value)) {
                                        valueSame = value.equals(comparisonCarItems.get(i).getName());
                                    }
                                    value = comparisonCarItems.get(i).getName();
                                }
                            }
                            comparisonCarItems.get(0).setSame(valueSame);
                        }
                        if (contentData != null && isComparsionSame)//因为显示全部的时候 contentAllData  contentData  是同一对象
                            for (ArrayList<ComparisonCarItem> comparisonCarItems2 : contentData) {
                                comparisonCarItems2.remove(columnPosition);
                                //开始判断这一行的值是否全部相同
                                boolean valueSame = true;
                                String value = null;
                                for (int i = 1; i < comparisonCarItems2.size() - 1; i++) {
                                    if (valueSame) {
                                        if (!TextUtils.isEmpty(value)) {
                                            valueSame = value.equals(comparisonCarItems2.get(i).getName());
                                        }
                                        value = comparisonCarItems2.get(i).getName();
                                    }
                                }
                                comparisonCarItems2.get(0).setSame(valueSame);
                            }
                        setAdapter();
                        if (isComparsionSame) {
                            toggleData();
                        }
                    }
                });
                tvTitle.setWidth(titleWidth);
            }
            tvTitle.setText(comparisonCarItem.getName());
            if (comparisonCarItem.getImgBackgroud() != -1) {//添加按钮
                imgAdd.setVisibility(View.VISIBLE);
                if (getContentColumn() - 2 == 9) {//最多九列"比较数据"  或者 来自车系比较
                    imgAdd.setImageDrawable(null);
                    imgAdd.setBackgroundDrawable(null);
                    imgAdd.setImageResource(R.drawable.icon_notianjia);
                    imgAdd.setOnClickListener(null);
                } else {
                    imgAdd.setImageResource(comparisonCarItem.getImgBackgroud());
                    imgAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            gotoHere(MainActivity.this, titleData.size() - 2);
                            Toast.makeText(MainActivity.this, "添加数据", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                imgAdd.setVisibility(View.GONE);
                imgAdd.setEnabled(false);
            }
            tvTitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            tvTitle.setPadding(titleLeftPadding, 0, 0, 0);
            return view;
        }

        //表格正文的view，行和列都从0开始，宽度的话在载入的时候，默认会是以标题行各列的宽度，高度的话自适应
        @Override
        public View getTableCellView(int contentRow, int contentColum, View view, ViewGroup parent) {
            TableCellView tableCellView = null;
            if (null == view) {
                tableCellView = new TableCellView();
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_comparison_header, null);
                tableCellView.flContent = (FrameLayout) view.findViewById(R.id.fl_content);
                tableCellView.tvTitle = (TextView) view.findViewById(R.id.tv_title);
                tableCellView.img_add = ((ImageView) view.findViewById(R.id.img_add));
                tableCellView.img_del = ((ImageView) view.findViewById(R.id.img_del));
                tableCellView.img_rank = ((ImageView) view.findViewById(R.id.iv_item_rank_pic));
                tableCellView.img_add.setVisibility(View.GONE);
                tableCellView.img_del.setVisibility(View.GONE);
                tableCellView.img_rank.setVisibility(View.GONE);
                tableCellView.tvTitle.setMinHeight(cellHieght);
                tableCellView.tvTitle.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tableCellView.tvTitle.setGravity(Gravity.CENTER);
                view.setTag(tableCellView);
            } else {
                tableCellView = (TableCellView) view.getTag();
            }
            ArrayList<ComparisonCarItem> comparisonCarItems = contentAllData.get(contentRow);
            ComparisonCarItem comparisonCarItem = comparisonCarItems.get(contentColum);
            int size = comparisonCarItems.size();
            boolean same = comparisonCarItems.get(0).isSame();

            if (!tableCellView.tvTitle.getText().equals(comparisonCarItem.getName())) {
                tableCellView.tvTitle.setText(comparisonCarItem.getName());
            }

            boolean isFirstColum = contentColum == 0;
            boolean isFirstOrLastColum = isFirstColum || contentColum == size - 1;

            int color = getResources().getColor(isFirstColum ? R.color.black_gray : R.color.text_noraml_color);
            if (tableCellView.tvTitle.getCurrentTextColor() != color) {
                tableCellView.tvTitle.setTextColor(color);
            }


//            int tabColor = getResources().getColor(isFirstOrLastColum || contentRow != 0 ? R.color.text_noraml_color : R.color.red);
//            if (tableCellView.tvTitle.getCurrentTextColor() != tabColor) {
//                tableCellView.tvTitle.setTextColor(tabColor);
//            }

            int resid = isFirstOrLastColum || same ? R.drawable.bg_shape_gray : R.drawable.bg_shape_green;
            if (view.getTag() == null || view.getTag() != Integer.valueOf(resid)) {
                view.setBackgroundResource(resid);
                view.setTag(R.layout.layout_comparison_header, resid);
            }

//            int visibility = isComparsionSame && same ? View.GONE : View.VISIBLE;
//            if (tableCellView.flContent.getVisibility() != visibility) {
//                tableCellView.flContent.setVisibility(visibility);
//            }

            return view;
        }

        @Override
        public View getTableRowTitlrView(int contentRow, View view) {
            TableRowTitlrView tableRowTitlrView = null;
            if (null == view) {
                tableRowTitlrView = new TableRowTitlrView();
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_comparison_cell_header, null);
                tableRowTitlrView.tvTitle = (TextView) view.findViewById(R.id.tv_title);
                tableRowTitlrView.tvsubTitle = (TextView) view.findViewById(R.id.tv_subtitle);
            } else {
                tableRowTitlrView = (TableRowTitlrView) view.getTag();
            }
            ComparisonCarItem comparisonCarItem = contentAllData.get(contentRow).get(0);
            int visibility = comparisonCarItem.isHeader() ? View.VISIBLE : View.GONE;
            if (visibility != view.getVisibility()) {
                view.setVisibility(visibility);
            }
            if (!tableRowTitlrView.tvTitle.getText().equals(comparisonCarItem.getRowTitle())) {
                tableRowTitlrView.tvTitle.setText(comparisonCarItem.getRowTitle());
            }
            if (!tableRowTitlrView.tvsubTitle.getText().equals(CC)) {
                tableRowTitlrView.tvsubTitle.setText(CC);
            }
            return view;
        }

        @Override
        public View getFooterView(ListView view) {
            View footer = LayoutInflater.from(context).inflate(R.layout.layout_comparison_cell_footer, null);
            TextView tvTitle = (TextView) footer.findViewById(R.id.tv_title);
            TextView tvsubTitle = (TextView) footer.findViewById(R.id.tv_subtitle);
            footer.setBackgroundColor(getResources().getColor(R.color.white));
            tvTitle.setText("注:以上仅供参考,请以店内实车为准");
            tvTitle.setPadding(20, 20, 20, 20);
            tvTitle.setTextColor(getResources().getColor(R.color.text_selected));
            tvsubTitle.setText(null);
            return footer;
        }


        @Override
        public Object getItem(int contentRow) {
            return contentAllData.get(contentRow);
        }


        //每一行被点击的时候的回调
        @Override
        public void OnClickContentRowItem(int row, View convertView) {
        }

        class TableCellView {
            FrameLayout flContent;
            TextView tvTitle;
            ImageView img_add;
            ImageView img_del;
            ImageView img_rank;
        }

        class TableRowTitlrView {
            TextView tvTitle;
            TextView tvsubTitle;
        }

    }


    private final String json = "{\"param\":[{\"name\":\"基本参数\",\"paramitems\":[{\"name\":\"车型名称\",\"valueitems\":[{\"specid\":25379,\"value\":\"POLO 2016款 1.4L 手动风尚型\"},{\"specid\":25381,\"value\":\"POLO 2016款 1.6L 手动舒适型\"},{\"specid\":25390,\"value\":\"POLO 2016款 1.4TSI GTI\"},{\"specid\":25382,\"value\":\"POLO 2016款 1.6L 自动舒适型\"}]},{\"name\":\"厂商指导价(元)\",\"valueitems\":[{\"specid\":25379,\"value\":\"7.59万\"},{\"specid\":25381,\"value\":\"9.19万\"},{\"specid\":25390,\"value\":\"14.69万\"},{\"specid\":25382,\"value\":\"10.39万\"}]},{\"name\":\"厂商\",\"valueitems\":[{\"specid\":25379,\"value\":\"上汽大众\"},{\"specid\":25381,\"value\":\"上汽大众\"},{\"specid\":25390,\"value\":\"上汽大众\"},{\"specid\":25382,\"value\":\"上汽大众\"}]},{\"name\":\"级别\",\"valueitems\":[{\"specid\":25379,\"value\":\"小型车\"},{\"specid\":25381,\"value\":\"小型车\"},{\"specid\":25390,\"value\":\"小型车\"},{\"specid\":25382,\"value\":\"小型车\"}]},{\"name\":\"发动机\",\"valueitems\":[{\"specid\":25379,\"value\":\"1.4L 90马力 L4\"},{\"specid\":25381,\"value\":\"1.6L 110马力 L4\"},{\"specid\":25390,\"value\":\"1.4T 150马力 L4\"},{\"specid\":25382,\"value\":\"1.6L 110马力 L4\"}]},{\"name\":\"变速箱\",\"valueitems\":[{\"specid\":25379,\"value\":\"5挡手动\"},{\"specid\":25381,\"value\":\"5挡手动\"},{\"specid\":25390,\"value\":\"7挡双离合\"},{\"specid\":25382,\"value\":\"6挡自动\"}]},{\"name\":\"长*宽*高(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"3970*1682*1462\"},{\"specid\":25381,\"value\":\"3970*1682*1462\"},{\"specid\":25390,\"value\":\"3975*1682*1487\"},{\"specid\":25382,\"value\":\"3970*1682*1462\"}]},{\"name\":\"车身结构\",\"valueitems\":[{\"specid\":25379,\"value\":\"5门5座两厢车\"},{\"specid\":25381,\"value\":\"5门5座两厢车\"},{\"specid\":25390,\"value\":\"5门5座两厢车\"},{\"specid\":25382,\"value\":\"5门5座两厢车\"}]},{\"name\":\"最高车速(km/h)\",\"valueitems\":[{\"specid\":25379,\"value\":\"182\"},{\"specid\":25381,\"value\":\"185\"},{\"specid\":25390,\"value\":\"205\"},{\"specid\":25382,\"value\":\"185\"}]},{\"name\":\"官方0-100km/h加速(s)\",\"valueitems\":[{\"specid\":25379,\"value\":\"12.2\"},{\"specid\":25381,\"value\":\"10.6\"},{\"specid\":25390,\"value\":\"8.3\"},{\"specid\":25382,\"value\":\"11.8\"}]},{\"name\":\"实测0-100km/h加速(s)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"实测100-0km/h制动(m)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"实测油耗(L/100km)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"工信部综合油耗(L/100km)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"实测离地间隙(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"整车质保\",\"valueitems\":[{\"specid\":25379,\"value\":\"三年或10万公里\"},{\"specid\":25381,\"value\":\"三年或10万公里\"},{\"specid\":25390,\"value\":\"三年或10万公里\"},{\"specid\":25382,\"value\":\"三年或10万公里\"}]}]},{\"name\":\"车身\",\"paramitems\":[{\"name\":\"长度(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"3970\"},{\"specid\":25381,\"value\":\"3970\"},{\"specid\":25390,\"value\":\"3975\"},{\"specid\":25382,\"value\":\"3970\"}]},{\"name\":\"宽度(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"1682\"},{\"specid\":25381,\"value\":\"1682\"},{\"specid\":25390,\"value\":\"1682\"},{\"specid\":25382,\"value\":\"1682\"}]},{\"name\":\"高度(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"1462\"},{\"specid\":25381,\"value\":\"1462\"},{\"specid\":25390,\"value\":\"1487\"},{\"specid\":25382,\"value\":\"1462\"}]},{\"name\":\"轴距(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"2470\"},{\"specid\":25381,\"value\":\"2470\"},{\"specid\":25390,\"value\":\"2470\"},{\"specid\":25382,\"value\":\"2470\"}]},{\"name\":\"前轮距(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后轮距(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"最小离地间隙(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"整备质量(kg)\",\"valueitems\":[{\"specid\":25379,\"value\":\"1060\"},{\"specid\":25381,\"value\":\"1075\"},{\"specid\":25390,\"value\":\"1207\"},{\"specid\":25382,\"value\":\"1120\"}]},{\"name\":\"车身结构\",\"valueitems\":[{\"specid\":25379,\"value\":\"两厢车\"},{\"specid\":25381,\"value\":\"两厢车\"},{\"specid\":25390,\"value\":\"两厢车\"},{\"specid\":25382,\"value\":\"两厢车\"}]},{\"name\":\"车门数(个)\",\"valueitems\":[{\"specid\":25379,\"value\":\"5\"},{\"specid\":25381,\"value\":\"5\"},{\"specid\":25390,\"value\":\"5\"},{\"specid\":25382,\"value\":\"5\"}]},{\"name\":\"座位数(个)\",\"valueitems\":[{\"specid\":25379,\"value\":\"5\"},{\"specid\":25381,\"value\":\"5\"},{\"specid\":25390,\"value\":\"5\"},{\"specid\":25382,\"value\":\"5\"}]},{\"name\":\"油箱容积(L)\",\"valueitems\":[{\"specid\":25379,\"value\":\"45\"},{\"specid\":25381,\"value\":\"45\"},{\"specid\":25390,\"value\":\"45\"},{\"specid\":25382,\"value\":\"45\"}]},{\"name\":\"行李厢容积(L)\",\"valueitems\":[{\"specid\":25379,\"value\":\"250\"},{\"specid\":25381,\"value\":\"250\"},{\"specid\":25390,\"value\":\"250\"},{\"specid\":25382,\"value\":\"250\"}]}]},{\"name\":\"发动机\",\"paramitems\":[{\"name\":\"发动机型号\",\"valueitems\":[{\"specid\":25379,\"value\":\"EA211\"},{\"specid\":25381,\"value\":\"EA211\"},{\"specid\":25390,\"value\":\"EA211\"},{\"specid\":25382,\"value\":\"EA211\"}]},{\"name\":\"排量(mL)\",\"valueitems\":[{\"specid\":25379,\"value\":\"1395\"},{\"specid\":25381,\"value\":\"1598\"},{\"specid\":25390,\"value\":\"1395\"},{\"specid\":25382,\"value\":\"1598\"}]},{\"name\":\"进气形式\",\"valueitems\":[{\"specid\":25379,\"value\":\"自然吸气\"},{\"specid\":25381,\"value\":\"自然吸气\"},{\"specid\":25390,\"value\":\"涡轮增压\"},{\"specid\":25382,\"value\":\"自然吸气\"}]},{\"name\":\"气缸排列形式\",\"valueitems\":[{\"specid\":25379,\"value\":\"L\"},{\"specid\":25381,\"value\":\"L\"},{\"specid\":25390,\"value\":\"L\"},{\"specid\":25382,\"value\":\"L\"}]},{\"name\":\"气缸数(个)\",\"valueitems\":[{\"specid\":25379,\"value\":\"4\"},{\"specid\":25381,\"value\":\"4\"},{\"specid\":25390,\"value\":\"4\"},{\"specid\":25382,\"value\":\"4\"}]},{\"name\":\"每缸气门数(个)\",\"valueitems\":[{\"specid\":25379,\"value\":\"4\"},{\"specid\":25381,\"value\":\"4\"},{\"specid\":25390,\"value\":\"4\"},{\"specid\":25382,\"value\":\"4\"}]},{\"name\":\"压缩比\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"配气机构\",\"valueitems\":[{\"specid\":25379,\"value\":\"DOHC\"},{\"specid\":25381,\"value\":\"DOHC\"},{\"specid\":25390,\"value\":\"DOHC\"},{\"specid\":25382,\"value\":\"DOHC\"}]},{\"name\":\"缸径(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"行程(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"最大马力(Ps)\",\"valueitems\":[{\"specid\":25379,\"value\":\"90\"},{\"specid\":25381,\"value\":\"110\"},{\"specid\":25390,\"value\":\"150\"},{\"specid\":25382,\"value\":\"110\"}]},{\"name\":\"最大功率(kW)\",\"valueitems\":[{\"specid\":25379,\"value\":\"66\"},{\"specid\":25381,\"value\":\"81\"},{\"specid\":25390,\"value\":\"110\"},{\"specid\":25382,\"value\":\"81\"}]},{\"name\":\"最大功率转速(rpm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"5500\"},{\"specid\":25381,\"value\":\"5800\"},{\"specid\":25390,\"value\":\"5000\"},{\"specid\":25382,\"value\":\"5800\"}]},{\"name\":\"最大扭矩(N·m)\",\"valueitems\":[{\"specid\":25379,\"value\":\"132\"},{\"specid\":25381,\"value\":\"155\"},{\"specid\":25390,\"value\":\"250\"},{\"specid\":25382,\"value\":\"155\"}]},{\"name\":\"最大扭矩转速(rpm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"3800\"},{\"specid\":25381,\"value\":\"3800\"},{\"specid\":25390,\"value\":\"1750-3500\"},{\"specid\":25382,\"value\":\"3800\"}]},{\"name\":\"发动机特有技术\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"燃料形式\",\"valueitems\":[{\"specid\":25379,\"value\":\"汽油\"},{\"specid\":25381,\"value\":\"汽油\"},{\"specid\":25390,\"value\":\"汽油\"},{\"specid\":25382,\"value\":\"汽油\"}]},{\"name\":\"燃油标号\",\"valueitems\":[{\"specid\":25379,\"value\":\"93号(京92号)\"},{\"specid\":25381,\"value\":\"93号(京92号)\"},{\"specid\":25390,\"value\":\"97号(京95号)\"},{\"specid\":25382,\"value\":\"93号(京92号)\"}]},{\"name\":\"供油方式\",\"valueitems\":[{\"specid\":25379,\"value\":\"多点电喷\"},{\"specid\":25381,\"value\":\"多点电喷\"},{\"specid\":25390,\"value\":\"直喷\"},{\"specid\":25382,\"value\":\"多点电喷\"}]},{\"name\":\"缸盖材料\",\"valueitems\":[{\"specid\":25379,\"value\":\"铝\"},{\"specid\":25381,\"value\":\"铝\"},{\"specid\":25390,\"value\":\"铝\"},{\"specid\":25382,\"value\":\"铝\"}]},{\"name\":\"缸体材料\",\"valueitems\":[{\"specid\":25379,\"value\":\"铝\"},{\"specid\":25381,\"value\":\"铝\"},{\"specid\":25390,\"value\":\"铝\"},{\"specid\":25382,\"value\":\"铝\"}]},{\"name\":\"环保标准\",\"valueitems\":[{\"specid\":25379,\"value\":\"国V\"},{\"specid\":25381,\"value\":\"国V\"},{\"specid\":25390,\"value\":\"国V\"},{\"specid\":25382,\"value\":\"国V\"}]}]},{\"name\":\"变速箱\",\"paramitems\":[{\"name\":\"简称\",\"valueitems\":[{\"specid\":25379,\"value\":\"5挡手动\"},{\"specid\":25381,\"value\":\"5挡手动\"},{\"specid\":25390,\"value\":\"7挡双离合\"},{\"specid\":25382,\"value\":\"6挡自动\"}]},{\"name\":\"挡位个数\",\"valueitems\":[{\"specid\":25379,\"value\":\"5\"},{\"specid\":25381,\"value\":\"5\"},{\"specid\":25390,\"value\":\"7\"},{\"specid\":25382,\"value\":\"6\"}]},{\"name\":\"变速箱类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"手动变速箱(MT)\"},{\"specid\":25381,\"value\":\"手动变速箱(MT)\"},{\"specid\":25390,\"value\":\"双离合变速箱(DCT)\"},{\"specid\":25382,\"value\":\"自动变速箱(AT)\"}]}]},{\"name\":\"底盘转向\",\"paramitems\":[{\"name\":\"驱动方式\",\"valueitems\":[{\"specid\":25379,\"value\":\"前置前驱\"},{\"specid\":25381,\"value\":\"前置前驱\"},{\"specid\":25390,\"value\":\"前置前驱\"},{\"specid\":25382,\"value\":\"前置前驱\"}]},{\"name\":\"前悬架类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"麦弗逊式独立悬架\"},{\"specid\":25381,\"value\":\"麦弗逊式独立悬架\"},{\"specid\":25390,\"value\":\"麦弗逊式独立悬架\"},{\"specid\":25382,\"value\":\"麦弗逊式独立悬架\"}]},{\"name\":\"后悬架类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"扭力梁式非独立悬架\"},{\"specid\":25381,\"value\":\"扭力梁式非独立悬架\"},{\"specid\":25390,\"value\":\"扭力梁式非独立悬架\"},{\"specid\":25382,\"value\":\"扭力梁式非独立悬架\"}]},{\"name\":\"助力类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"电动助力\"},{\"specid\":25381,\"value\":\"电动助力\"},{\"specid\":25390,\"value\":\"电动助力\"},{\"specid\":25382,\"value\":\"电动助力\"}]},{\"name\":\"车体结构\",\"valueitems\":[{\"specid\":25379,\"value\":\"承载式\"},{\"specid\":25381,\"value\":\"承载式\"},{\"specid\":25390,\"value\":\"承载式\"},{\"specid\":25382,\"value\":\"承载式\"}]}]},{\"name\":\"车轮制动\",\"paramitems\":[{\"name\":\"前制动器类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"通风盘式\"},{\"specid\":25381,\"value\":\"通风盘式\"},{\"specid\":25390,\"value\":\"通风盘式\"},{\"specid\":25382,\"value\":\"通风盘式\"}]},{\"name\":\"后制动器类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"鼓式\"},{\"specid\":25381,\"value\":\"鼓式\"},{\"specid\":25390,\"value\":\"盘式\"},{\"specid\":25382,\"value\":\"鼓式\"}]},{\"name\":\"驻车制动类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"手刹\"},{\"specid\":25381,\"value\":\"手刹\"},{\"specid\":25390,\"value\":\"手刹\"},{\"specid\":25382,\"value\":\"手刹\"}]},{\"name\":\"前轮胎规格\",\"valueitems\":[{\"specid\":25379,\"value\":\"185/65 R14\"},{\"specid\":25381,\"value\":\"185/60 R15\"},{\"specid\":25390,\"value\":\"215/45 R16\"},{\"specid\":25382,\"value\":\"185/60 R15\"}]},{\"name\":\"后轮胎规格\",\"valueitems\":[{\"specid\":25379,\"value\":\"185/65 R14\"},{\"specid\":25381,\"value\":\"185/60 R15\"},{\"specid\":25390,\"value\":\"215/45 R16\"},{\"specid\":25382,\"value\":\"185/60 R15\"}]},{\"name\":\"备胎规格\",\"valueitems\":[{\"specid\":25379,\"value\":\"非全尺寸\"},{\"specid\":25381,\"value\":\"非全尺寸\"},{\"specid\":25390,\"value\":\"非全尺寸\"},{\"specid\":25382,\"value\":\"非全尺寸\"}]}]}],\"config\":[{\"name\":\"安全装备\",\"configitems\":[{\"name\":\"主/副驾驶座安全气囊\",\"valueitems\":[{\"specid\":25379,\"value\":\"主●\\u0026nbsp;/\\u0026nbsp;副●\"},{\"specid\":25381,\"value\":\"主●\\u0026nbsp;/\\u0026nbsp;副●\"},{\"specid\":25390,\"value\":\"主●\\u0026nbsp;/\\u0026nbsp;副●\"},{\"specid\":25382,\"value\":\"主●\\u0026nbsp;/\\u0026nbsp;副●\"}]},{\"name\":\"前/后排侧气囊\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后-\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后-\"},{\"specid\":25382,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后-\"}]},{\"name\":\"前/后排头部气囊(气帘)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"膝部气囊\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"胎压监测装置\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"零胎压继续行驶\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"安全带未系提示\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"ISOFIX儿童座椅接口\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"发动机电子防盗\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"车内中控锁\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"遥控钥匙\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"无钥匙启动系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"无钥匙进入系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"操控配置\",\"configitems\":[{\"name\":\"ABS防抱死\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"制动力分配(EBD/CBC等)\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"刹车辅助(EBA/BAS/BA等)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"牵引力控制(ASR/TCS/TRC等)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"车身稳定控制(ESC/ESP/DSC等)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"上坡辅助\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"自动驻车\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"陡坡缓降\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"可变悬架\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"空气悬架\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"可变转向比\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前桥限滑差速器/差速锁\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"中央差速器锁止功能\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后桥限滑差速器/差速锁\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"外部配置\",\"configitems\":[{\"name\":\"电动天窗\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"全景天窗\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"运动外观套件\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"铝合金轮圈\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"电动吸合门\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"侧滑门\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"电动后备厢\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"感应后备厢\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"车顶行李架\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"内部配置\",\"configitems\":[{\"name\":\"真皮方向盘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"方向盘调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"上下调节\"},{\"specid\":25381,\"value\":\"上下调节\"},{\"specid\":25390,\"value\":\"上下调节\"},{\"specid\":25382,\"value\":\"上下调节\"}]},{\"name\":\"方向盘电动调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"多功能方向盘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"方向盘换挡\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"方向盘加热\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"方向盘记忆\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"定速巡航\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"前/后驻车雷达\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"前○\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25382,\"value\":\"前○\\u0026nbsp;/\\u0026nbsp;后●\"}]},{\"name\":\"倒车视频影像\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"行车电脑显示屏\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"全液晶仪表盘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"HUD抬头数字显示\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"座椅配置\",\"configitems\":[{\"name\":\"座椅材质\",\"valueitems\":[{\"specid\":25379,\"value\":\"织物\"},{\"specid\":25381,\"value\":\"织物\"},{\"specid\":25390,\"value\":\"皮/织物混搭\"},{\"specid\":25382,\"value\":\"织物\"}]},{\"name\":\"运动风格座椅\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"座椅高低调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"腰部支撑调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"肩部支撑调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"主/副驾驶座电动调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"第二排靠背角度调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"第二排座椅移动\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排座椅电动调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"电动座椅记忆\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前/后排座椅加热\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前/后排座椅通风\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前/后排座椅按摩\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"第三排座椅\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排座椅放倒方式\",\"valueitems\":[{\"specid\":25379,\"value\":\"整体放倒\"},{\"specid\":25381,\"value\":\"比例放倒\"},{\"specid\":25390,\"value\":\"比例放倒\"},{\"specid\":25382,\"value\":\"比例放倒\"}]},{\"name\":\"前/后中央扶手\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排杯架\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]}]},{\"name\":\"多媒体配置\",\"configitems\":[{\"name\":\"GPS导航系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"定位互动服务\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"中控台彩色大屏\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"蓝牙/车载电话\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"车载电视\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排液晶屏\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"220V/230V电源\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"外接音源接口\",\"valueitems\":[{\"specid\":25379,\"value\":\"USB+AUX+SD卡插槽\"},{\"specid\":25381,\"value\":\"USB+AUX+SD卡插槽\"},{\"specid\":25390,\"value\":\"USB+AUX+SD卡插槽\"},{\"specid\":25382,\"value\":\"USB+AUX+SD卡插槽\"}]},{\"name\":\"CD支持MP3/WMA\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"多媒体系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"单碟CD\"},{\"specid\":25390,\"value\":\"单碟CD\"},{\"specid\":25382,\"value\":\"单碟CD\"}]},{\"name\":\"扬声器品牌\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"扬声器数量\",\"valueitems\":[{\"specid\":25379,\"value\":\"4-5喇叭\"},{\"specid\":25381,\"value\":\"6-7喇叭\"},{\"specid\":25390,\"value\":\"6-7喇叭\"},{\"specid\":25382,\"value\":\"6-7喇叭\"}]}]},{\"name\":\"灯光配置\",\"configitems\":[{\"name\":\"近光灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"卤素\"},{\"specid\":25381,\"value\":\"卤素\"},{\"specid\":25390,\"value\":\"卤素(选装LED)\"},{\"specid\":25382,\"value\":\"卤素\"}]},{\"name\":\"远光灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"卤素\"},{\"specid\":25381,\"value\":\"卤素\"},{\"specid\":25390,\"value\":\"卤素(选装LED)\"},{\"specid\":25382,\"value\":\"卤素\"}]},{\"name\":\"日间行车灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"○\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"自适应远近光\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"自动头灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"转向辅助灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"转向头灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前雾灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"大灯高度可调\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"大灯清洗装置\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"○\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"车内氛围灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"玻璃/后视镜\",\"configitems\":[{\"name\":\"前/后电动车窗\",\"valueitems\":[{\"specid\":25379,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25381,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25382,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"}]},{\"name\":\"车窗防夹手功能\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"防紫外线/隔热玻璃\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"后视镜电动调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"后视镜加热\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"内/外后视镜自动防眩目\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后视镜电动折叠\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后视镜记忆\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后风挡遮阳帘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排侧遮阳帘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排侧隐私玻璃\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"遮阳板化妆镜\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"后雨刷\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"感应雨刷\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"空调/冰箱\",\"configitems\":[{\"name\":\"空调控制方式\",\"valueitems\":[{\"specid\":25379,\"value\":\"手动●\"},{\"specid\":25381,\"value\":\"手动●\"},{\"specid\":25390,\"value\":\"自动●\"},{\"specid\":25382,\"value\":\"手动●\"}]},{\"name\":\"后排独立空调\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后座出风口\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"温度分区控制\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"车内空气调节/花粉过滤\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"车载冰箱\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"高科技配置\",\"configitems\":[{\"name\":\"自动泊车入位\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"发动机启停技术\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"并线辅助\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"车道偏离预警系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"主动刹车/主动安全系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"整体主动转向系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"夜视系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"中控液晶屏分屏显示\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"自适应巡航\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"全景摄像头\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]}],\"search\":\"\\u003cdl\\u003e\\u003cdt\\u003e年\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e代\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e款：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e2016款\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e发\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e动\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e机：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e1.4L\\u003c/span\\u003e\\u003cspan class=\\\"item\\\"\\u003e1.4T\\u003c/span\\u003e\\u003cspan class=\\\"item\\\"\\u003e1.6L\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e车身结构：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e两厢车\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e座\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e位\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e数：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e5座\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e变\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e速\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e箱：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e手动\\u003c/span\\u003e\\u003cspan class=\\\"item\\\"\\u003e双离合\\u003c/span\\u003e\\u003cspan class=\\\"item\\\"\\u003e自动\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e驱动方式：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e两驱\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e能\\u003cem class=\\\"twoword\\\"\\u003e\\u003c/em\\u003e源：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e汽油\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\"}";
}
