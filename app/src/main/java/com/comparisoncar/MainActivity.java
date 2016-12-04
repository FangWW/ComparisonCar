package com.comparisoncar;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.comparisoncar.bean.CarComparisonBean;
import com.comparisoncar.bean.ComparisonCarItem;
import com.comparisoncar.vhtableview.VHBaseAdapter;
import com.comparisoncar.vhtableview.VHTableView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String CC = "●标配 ○选配 -无";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Gson gson = new Gson();
        CarComparisonBean carComparisonBean = gson.fromJson(json, CarComparisonBean.class);


        {
            List<CarComparisonBean.ParamEntity> data = carComparisonBean.getParam();

            //设置数据源
            final VHTableView vht_table = (VHTableView) findViewById(R.id.vht_table);

            ArrayList<ComparisonCarItem> titleData = new ArrayList<>();

            ComparisonCarItem comparisonCarItem = new ComparisonCarItem();
            comparisonCarItem.setName("");
            titleData.add(comparisonCarItem);

            List<CarComparisonBean.ParamEntity.ParamitemsEntity.ValueitemsEntity> paramitems = data.get(0).getParamitems().get(0).getValueitems();
            for (CarComparisonBean.ParamEntity.ParamitemsEntity.ValueitemsEntity paramitem : paramitems) {

                ComparisonCarItem comparisonCarItem2 = new ComparisonCarItem();
                comparisonCarItem2.setName(paramitem.getValue());
                comparisonCarItem2.setId(paramitem.getSpecid());
                titleData.add(comparisonCarItem2);
            }

            ComparisonCarItem comparisonCarItem3 = new ComparisonCarItem();
            comparisonCarItem3.setName("+");
            titleData.add(comparisonCarItem3);


            final ArrayList<ArrayList<ComparisonCarItem>> contentData = new ArrayList<>();
            for (CarComparisonBean.ParamEntity paramEntity : data) {

                for (CarComparisonBean.ParamEntity.ParamitemsEntity paramitemsEntity : paramEntity.getParamitems()) {
                    ArrayList<ComparisonCarItem> contentRowData = new ArrayList<>();

                    ComparisonCarItem comparisonCarItem4 = new ComparisonCarItem();
                    comparisonCarItem4.setName(paramitemsEntity.getName());
                    if (!contentData.isEmpty()) {//判断行标题
                        ComparisonCarItem lastComparisonCar = contentData.get(contentData.size() - 1).get(0);
                        comparisonCarItem4.setHeader(!lastComparisonCar.getRowTitle().equals(paramEntity.getName()));
                    }
                    comparisonCarItem4.setRowTitle(paramEntity.getName());
                    contentRowData.add(comparisonCarItem4);

                    //开始判断这一行的值是否全部相同
                    boolean valueSame = true;
                    String value = null;
                    for (CarComparisonBean.ParamEntity.ParamitemsEntity.ValueitemsEntity valueitemsEntity : paramitemsEntity.getValueitems()) {
                        ComparisonCarItem comparisonCarItem5 = new ComparisonCarItem();
                        comparisonCarItem5.setName(valueitemsEntity.getValue());
                        comparisonCarItem5.setId(valueitemsEntity.getSpecid());
                        contentRowData.add(comparisonCarItem5);
                        if (valueSame) {
                            if (!TextUtils.isEmpty(value)) {
                                valueSame = value.equals(comparisonCarItem5.getName());
                            }
                            value = comparisonCarItem5.getName();
                        }
                    }
                    comparisonCarItem4.setSame(valueSame);

                    ComparisonCarItem comparisonCarItem6 = new ComparisonCarItem();
                    comparisonCarItem6.setName("-");
                    contentRowData.add(comparisonCarItem6);

                    contentData.add(contentRowData);
                }

            }
            //● ○
            VHTableAdapter tableAdapter = new VHTableAdapter(MainActivity.this, titleData, contentData);
//                    vht_table.setFirstColumnIsMove(true);//设置第一列是否可移动,默认不可移动
//                    vht_table.setShowTitle(false);//设置是否显示标题行,默认显示
            //一般表格都只是展示用的，所以这里没做刷新，真要刷新数据的话，重新setadaper一次吧
            vht_table.setAdapter(tableAdapter);

            View suspensionView = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_comparison_cell_header, null);
            final TextView tvSuspension = (TextView) suspensionView.findViewById(R.id.tv_title);
            final TextView tvsubTitle = (TextView) suspensionView.findViewById(R.id.tv_subtitle);
            tvsubTitle.setText(CC);

            vht_table.addTitleLayout(suspensionView);

            vht_table.getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
                public int lastFirstVisibleItem;

//                        public int getSectionForPosition(int position) {
//                            if (contentData.size() == 0) {
//                                return 0;
//                            } else {
//                                return contentData.get(position).get(0).getName();
//                            }
//                        }
//
//                        public int getPositionForSection(int section) {
//                            for (int i = 0; i < contentData.size(); i++) {
//                                String sortStr = contentData.get(i).getSort_name();
//                                char firstChar = sortStr.toUpperCase().charAt(0);
//                                if (firstChar == section) {
//                                    return i;
//                                }
//                            }
//                            return -1;
//                        }

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    Log.i("w", "" + firstVisibleItem + "==" + visibleItemCount + "===" + totalItemCount);
                    String rowTitle = contentData.get(firstVisibleItem).get(0).getRowTitle();
                    String nextRowTitle = contentData.get(firstVisibleItem + 1).get(0).getRowTitle();

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
                    lastFirstVisibleItem = firstVisibleItem;
                }
            });
        }
    }

    class VHTableAdapter implements VHBaseAdapter {
        private Context context;
        private ArrayList<ComparisonCarItem> titleData;
        private ArrayList<ArrayList<ComparisonCarItem>> dataList;
        private int title0Width = 100;
        private int titleWidth = 180;
        private int titleHieght = 120;
        private int cellHieght = 80;
        private int fontSize = 11;


        public VHTableAdapter(Context context, ArrayList<ComparisonCarItem> titleData, ArrayList<ArrayList<ComparisonCarItem>> dataList) {
            this.context = context;
            this.titleData = titleData;
            this.dataList = dataList;
        }

        //表格内容的行数，不包括标题行
        @Override
        public int getContentRows() {
            return dataList.size();
        }

        //列数
        @Override
        public int getContentColumn() {
            return titleData.size();
        }

        //标题的view，这里从0开始，这里要注意，一定要有view返回去，不能为null，每一行
        // 各列的宽度就等于标题行的列的宽度，且边框的话，自己在这里和下文的表格单元格view里面设置
        @Override
        public View getTitleView(int columnPosition, ViewGroup parent) {
            FrameLayout view = (FrameLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_comparison_header, null);
            view.setMinimumWidth(titleHieght);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            ImageView imgDel = (ImageView) view.findViewById(R.id.img_del);
            tvTitle.setTextSize(fontSize);
            tvTitle.setHeight(titleHieght);
            view.setBackgroundResource(R.drawable.bg_shape_gray);
            if (0 == columnPosition) {
                imgDel.setVisibility(View.GONE);
                tvTitle.setWidth(title0Width);
            } else {
                imgDel.setVisibility(View.VISIBLE);
                imgDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                tvTitle.setWidth(titleWidth);
            }
            tvTitle.setText(titleData.get(columnPosition).getName());
            tvTitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            return view;
        }

        //表格正文的view，行和列都从0开始，宽度的话在载入的时候，默认会是以标题行各列的宽度，高度的话自适应
        @Override
        public View getTableCellView(int contentRow, int contentColum, View view, ViewGroup parent) {
            if (null == view) {
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_comparison_header, null);
            }
            FrameLayout flContent = (FrameLayout) view.findViewById(R.id.fl_content);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            ImageView imgDel = (ImageView) view.findViewById(R.id.img_del);
            tvTitle.setTextSize(fontSize);
            tvTitle.setHeight(cellHieght);
            imgDel.setVisibility(View.GONE);
            tvTitle.setText(dataList.get(contentRow).get(contentColum).getName());
            tvTitle.setGravity(Gravity.CENTER);
            view.setBackgroundResource(contentColum == 0 || contentColum == dataList.get(contentRow).size() - 1 || dataList.get(contentRow).get(0).isSame() ? R.drawable.bg_shape_gray : R.drawable.bg_shape_green);
            return view;
        }

        @Override
        public View getTableRowTitlrView(int contentRow, View view) {
            if (null == view) {
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_comparison_cell_header, null);
            }
            view.setVisibility(dataList.get(contentRow).get(0).isHeader() ? View.VISIBLE : View.GONE);
            TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
            TextView tvsubTitle = (TextView) view.findViewById(R.id.tv_subtitle);
            tvTitle.setText(dataList.get(contentRow).get(0).getRowTitle());
            tvsubTitle.setText("●标配 ○选配 -无");
            return view;
        }


        @Override
        public Object getItem(int contentRow) {
            return dataList.get(contentRow);
        }


        //每一行被点击的时候的回调
        @Override
        public void OnClickContentRowItem(int row, View convertView) {
        }


    }

    private final String json = "{\"param\":[{\"name\":\"基本参数\",\"paramitems\":[{\"name\":\"车型名称\",\"valueitems\":[{\"specid\":25379,\"value\":\"POLO 2016款 1.4L 手动风尚型\"},{\"specid\":25381,\"value\":\"POLO 2016款 1.6L 手动舒适型\"},{\"specid\":25390,\"value\":\"POLO 2016款 1.4TSI GTI\"},{\"specid\":25382,\"value\":\"POLO 2016款 1.6L 自动舒适型\"}]},{\"name\":\"厂商指导价(元)\",\"valueitems\":[{\"specid\":25379,\"value\":\"7.59万\"},{\"specid\":25381,\"value\":\"9.19万\"},{\"specid\":25390,\"value\":\"14.69万\"},{\"specid\":25382,\"value\":\"10.39万\"}]},{\"name\":\"厂商\",\"valueitems\":[{\"specid\":25379,\"value\":\"上汽大众\"},{\"specid\":25381,\"value\":\"上汽大众\"},{\"specid\":25390,\"value\":\"上汽大众\"},{\"specid\":25382,\"value\":\"上汽大众\"}]},{\"name\":\"级别\",\"valueitems\":[{\"specid\":25379,\"value\":\"小型车\"},{\"specid\":25381,\"value\":\"小型车\"},{\"specid\":25390,\"value\":\"小型车\"},{\"specid\":25382,\"value\":\"小型车\"}]},{\"name\":\"发动机\",\"valueitems\":[{\"specid\":25379,\"value\":\"1.4L 90马力 L4\"},{\"specid\":25381,\"value\":\"1.6L 110马力 L4\"},{\"specid\":25390,\"value\":\"1.4T 150马力 L4\"},{\"specid\":25382,\"value\":\"1.6L 110马力 L4\"}]},{\"name\":\"变速箱\",\"valueitems\":[{\"specid\":25379,\"value\":\"5挡手动\"},{\"specid\":25381,\"value\":\"5挡手动\"},{\"specid\":25390,\"value\":\"7挡双离合\"},{\"specid\":25382,\"value\":\"6挡自动\"}]},{\"name\":\"长*宽*高(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"3970*1682*1462\"},{\"specid\":25381,\"value\":\"3970*1682*1462\"},{\"specid\":25390,\"value\":\"3975*1682*1487\"},{\"specid\":25382,\"value\":\"3970*1682*1462\"}]},{\"name\":\"车身结构\",\"valueitems\":[{\"specid\":25379,\"value\":\"5门5座两厢车\"},{\"specid\":25381,\"value\":\"5门5座两厢车\"},{\"specid\":25390,\"value\":\"5门5座两厢车\"},{\"specid\":25382,\"value\":\"5门5座两厢车\"}]},{\"name\":\"最高车速(km/h)\",\"valueitems\":[{\"specid\":25379,\"value\":\"182\"},{\"specid\":25381,\"value\":\"185\"},{\"specid\":25390,\"value\":\"205\"},{\"specid\":25382,\"value\":\"185\"}]},{\"name\":\"官方0-100km/h加速(s)\",\"valueitems\":[{\"specid\":25379,\"value\":\"12.2\"},{\"specid\":25381,\"value\":\"10.6\"},{\"specid\":25390,\"value\":\"8.3\"},{\"specid\":25382,\"value\":\"11.8\"}]},{\"name\":\"实测0-100km/h加速(s)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"实测100-0km/h制动(m)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"实测油耗(L/100km)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"工信部综合油耗(L/100km)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"实测离地间隙(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"整车质保\",\"valueitems\":[{\"specid\":25379,\"value\":\"三年或10万公里\"},{\"specid\":25381,\"value\":\"三年或10万公里\"},{\"specid\":25390,\"value\":\"三年或10万公里\"},{\"specid\":25382,\"value\":\"三年或10万公里\"}]}]},{\"name\":\"车身\",\"paramitems\":[{\"name\":\"长度(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"3970\"},{\"specid\":25381,\"value\":\"3970\"},{\"specid\":25390,\"value\":\"3975\"},{\"specid\":25382,\"value\":\"3970\"}]},{\"name\":\"宽度(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"1682\"},{\"specid\":25381,\"value\":\"1682\"},{\"specid\":25390,\"value\":\"1682\"},{\"specid\":25382,\"value\":\"1682\"}]},{\"name\":\"高度(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"1462\"},{\"specid\":25381,\"value\":\"1462\"},{\"specid\":25390,\"value\":\"1487\"},{\"specid\":25382,\"value\":\"1462\"}]},{\"name\":\"轴距(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"2470\"},{\"specid\":25381,\"value\":\"2470\"},{\"specid\":25390,\"value\":\"2470\"},{\"specid\":25382,\"value\":\"2470\"}]},{\"name\":\"前轮距(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后轮距(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"最小离地间隙(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"整备质量(kg)\",\"valueitems\":[{\"specid\":25379,\"value\":\"1060\"},{\"specid\":25381,\"value\":\"1075\"},{\"specid\":25390,\"value\":\"1207\"},{\"specid\":25382,\"value\":\"1120\"}]},{\"name\":\"车身结构\",\"valueitems\":[{\"specid\":25379,\"value\":\"两厢车\"},{\"specid\":25381,\"value\":\"两厢车\"},{\"specid\":25390,\"value\":\"两厢车\"},{\"specid\":25382,\"value\":\"两厢车\"}]},{\"name\":\"车门数(个)\",\"valueitems\":[{\"specid\":25379,\"value\":\"5\"},{\"specid\":25381,\"value\":\"5\"},{\"specid\":25390,\"value\":\"5\"},{\"specid\":25382,\"value\":\"5\"}]},{\"name\":\"座位数(个)\",\"valueitems\":[{\"specid\":25379,\"value\":\"5\"},{\"specid\":25381,\"value\":\"5\"},{\"specid\":25390,\"value\":\"5\"},{\"specid\":25382,\"value\":\"5\"}]},{\"name\":\"油箱容积(L)\",\"valueitems\":[{\"specid\":25379,\"value\":\"45\"},{\"specid\":25381,\"value\":\"45\"},{\"specid\":25390,\"value\":\"45\"},{\"specid\":25382,\"value\":\"45\"}]},{\"name\":\"行李厢容积(L)\",\"valueitems\":[{\"specid\":25379,\"value\":\"250\"},{\"specid\":25381,\"value\":\"250\"},{\"specid\":25390,\"value\":\"250\"},{\"specid\":25382,\"value\":\"250\"}]}]},{\"name\":\"发动机\",\"paramitems\":[{\"name\":\"发动机型号\",\"valueitems\":[{\"specid\":25379,\"value\":\"EA211\"},{\"specid\":25381,\"value\":\"EA211\"},{\"specid\":25390,\"value\":\"EA211\"},{\"specid\":25382,\"value\":\"EA211\"}]},{\"name\":\"排量(mL)\",\"valueitems\":[{\"specid\":25379,\"value\":\"1395\"},{\"specid\":25381,\"value\":\"1598\"},{\"specid\":25390,\"value\":\"1395\"},{\"specid\":25382,\"value\":\"1598\"}]},{\"name\":\"进气形式\",\"valueitems\":[{\"specid\":25379,\"value\":\"自然吸气\"},{\"specid\":25381,\"value\":\"自然吸气\"},{\"specid\":25390,\"value\":\"涡轮增压\"},{\"specid\":25382,\"value\":\"自然吸气\"}]},{\"name\":\"气缸排列形式\",\"valueitems\":[{\"specid\":25379,\"value\":\"L\"},{\"specid\":25381,\"value\":\"L\"},{\"specid\":25390,\"value\":\"L\"},{\"specid\":25382,\"value\":\"L\"}]},{\"name\":\"气缸数(个)\",\"valueitems\":[{\"specid\":25379,\"value\":\"4\"},{\"specid\":25381,\"value\":\"4\"},{\"specid\":25390,\"value\":\"4\"},{\"specid\":25382,\"value\":\"4\"}]},{\"name\":\"每缸气门数(个)\",\"valueitems\":[{\"specid\":25379,\"value\":\"4\"},{\"specid\":25381,\"value\":\"4\"},{\"specid\":25390,\"value\":\"4\"},{\"specid\":25382,\"value\":\"4\"}]},{\"name\":\"压缩比\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"配气机构\",\"valueitems\":[{\"specid\":25379,\"value\":\"DOHC\"},{\"specid\":25381,\"value\":\"DOHC\"},{\"specid\":25390,\"value\":\"DOHC\"},{\"specid\":25382,\"value\":\"DOHC\"}]},{\"name\":\"缸径(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"行程(mm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"最大马力(Ps)\",\"valueitems\":[{\"specid\":25379,\"value\":\"90\"},{\"specid\":25381,\"value\":\"110\"},{\"specid\":25390,\"value\":\"150\"},{\"specid\":25382,\"value\":\"110\"}]},{\"name\":\"最大功率(kW)\",\"valueitems\":[{\"specid\":25379,\"value\":\"66\"},{\"specid\":25381,\"value\":\"81\"},{\"specid\":25390,\"value\":\"110\"},{\"specid\":25382,\"value\":\"81\"}]},{\"name\":\"最大功率转速(rpm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"5500\"},{\"specid\":25381,\"value\":\"5800\"},{\"specid\":25390,\"value\":\"5000\"},{\"specid\":25382,\"value\":\"5800\"}]},{\"name\":\"最大扭矩(N·m)\",\"valueitems\":[{\"specid\":25379,\"value\":\"132\"},{\"specid\":25381,\"value\":\"155\"},{\"specid\":25390,\"value\":\"250\"},{\"specid\":25382,\"value\":\"155\"}]},{\"name\":\"最大扭矩转速(rpm)\",\"valueitems\":[{\"specid\":25379,\"value\":\"3800\"},{\"specid\":25381,\"value\":\"3800\"},{\"specid\":25390,\"value\":\"1750-3500\"},{\"specid\":25382,\"value\":\"3800\"}]},{\"name\":\"发动机特有技术\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"燃料形式\",\"valueitems\":[{\"specid\":25379,\"value\":\"汽油\"},{\"specid\":25381,\"value\":\"汽油\"},{\"specid\":25390,\"value\":\"汽油\"},{\"specid\":25382,\"value\":\"汽油\"}]},{\"name\":\"燃油标号\",\"valueitems\":[{\"specid\":25379,\"value\":\"93号(京92号)\"},{\"specid\":25381,\"value\":\"93号(京92号)\"},{\"specid\":25390,\"value\":\"97号(京95号)\"},{\"specid\":25382,\"value\":\"93号(京92号)\"}]},{\"name\":\"供油方式\",\"valueitems\":[{\"specid\":25379,\"value\":\"多点电喷\"},{\"specid\":25381,\"value\":\"多点电喷\"},{\"specid\":25390,\"value\":\"直喷\"},{\"specid\":25382,\"value\":\"多点电喷\"}]},{\"name\":\"缸盖材料\",\"valueitems\":[{\"specid\":25379,\"value\":\"铝\"},{\"specid\":25381,\"value\":\"铝\"},{\"specid\":25390,\"value\":\"铝\"},{\"specid\":25382,\"value\":\"铝\"}]},{\"name\":\"缸体材料\",\"valueitems\":[{\"specid\":25379,\"value\":\"铝\"},{\"specid\":25381,\"value\":\"铝\"},{\"specid\":25390,\"value\":\"铝\"},{\"specid\":25382,\"value\":\"铝\"}]},{\"name\":\"环保标准\",\"valueitems\":[{\"specid\":25379,\"value\":\"国V\"},{\"specid\":25381,\"value\":\"国V\"},{\"specid\":25390,\"value\":\"国V\"},{\"specid\":25382,\"value\":\"国V\"}]}]},{\"name\":\"变速箱\",\"paramitems\":[{\"name\":\"简称\",\"valueitems\":[{\"specid\":25379,\"value\":\"5挡手动\"},{\"specid\":25381,\"value\":\"5挡手动\"},{\"specid\":25390,\"value\":\"7挡双离合\"},{\"specid\":25382,\"value\":\"6挡自动\"}]},{\"name\":\"挡位个数\",\"valueitems\":[{\"specid\":25379,\"value\":\"5\"},{\"specid\":25381,\"value\":\"5\"},{\"specid\":25390,\"value\":\"7\"},{\"specid\":25382,\"value\":\"6\"}]},{\"name\":\"变速箱类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"手动变速箱(MT)\"},{\"specid\":25381,\"value\":\"手动变速箱(MT)\"},{\"specid\":25390,\"value\":\"双离合变速箱(DCT)\"},{\"specid\":25382,\"value\":\"自动变速箱(AT)\"}]}]},{\"name\":\"底盘转向\",\"paramitems\":[{\"name\":\"驱动方式\",\"valueitems\":[{\"specid\":25379,\"value\":\"前置前驱\"},{\"specid\":25381,\"value\":\"前置前驱\"},{\"specid\":25390,\"value\":\"前置前驱\"},{\"specid\":25382,\"value\":\"前置前驱\"}]},{\"name\":\"前悬架类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"麦弗逊式独立悬架\"},{\"specid\":25381,\"value\":\"麦弗逊式独立悬架\"},{\"specid\":25390,\"value\":\"麦弗逊式独立悬架\"},{\"specid\":25382,\"value\":\"麦弗逊式独立悬架\"}]},{\"name\":\"后悬架类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"扭力梁式非独立悬架\"},{\"specid\":25381,\"value\":\"扭力梁式非独立悬架\"},{\"specid\":25390,\"value\":\"扭力梁式非独立悬架\"},{\"specid\":25382,\"value\":\"扭力梁式非独立悬架\"}]},{\"name\":\"助力类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"电动助力\"},{\"specid\":25381,\"value\":\"电动助力\"},{\"specid\":25390,\"value\":\"电动助力\"},{\"specid\":25382,\"value\":\"电动助力\"}]},{\"name\":\"车体结构\",\"valueitems\":[{\"specid\":25379,\"value\":\"承载式\"},{\"specid\":25381,\"value\":\"承载式\"},{\"specid\":25390,\"value\":\"承载式\"},{\"specid\":25382,\"value\":\"承载式\"}]}]},{\"name\":\"车轮制动\",\"paramitems\":[{\"name\":\"前制动器类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"通风盘式\"},{\"specid\":25381,\"value\":\"通风盘式\"},{\"specid\":25390,\"value\":\"通风盘式\"},{\"specid\":25382,\"value\":\"通风盘式\"}]},{\"name\":\"后制动器类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"鼓式\"},{\"specid\":25381,\"value\":\"鼓式\"},{\"specid\":25390,\"value\":\"盘式\"},{\"specid\":25382,\"value\":\"鼓式\"}]},{\"name\":\"驻车制动类型\",\"valueitems\":[{\"specid\":25379,\"value\":\"手刹\"},{\"specid\":25381,\"value\":\"手刹\"},{\"specid\":25390,\"value\":\"手刹\"},{\"specid\":25382,\"value\":\"手刹\"}]},{\"name\":\"前轮胎规格\",\"valueitems\":[{\"specid\":25379,\"value\":\"185/65 R14\"},{\"specid\":25381,\"value\":\"185/60 R15\"},{\"specid\":25390,\"value\":\"215/45 R16\"},{\"specid\":25382,\"value\":\"185/60 R15\"}]},{\"name\":\"后轮胎规格\",\"valueitems\":[{\"specid\":25379,\"value\":\"185/65 R14\"},{\"specid\":25381,\"value\":\"185/60 R15\"},{\"specid\":25390,\"value\":\"215/45 R16\"},{\"specid\":25382,\"value\":\"185/60 R15\"}]},{\"name\":\"备胎规格\",\"valueitems\":[{\"specid\":25379,\"value\":\"非全尺寸\"},{\"specid\":25381,\"value\":\"非全尺寸\"},{\"specid\":25390,\"value\":\"非全尺寸\"},{\"specid\":25382,\"value\":\"非全尺寸\"}]}]}],\"config\":[{\"name\":\"安全装备\",\"configitems\":[{\"name\":\"主/副驾驶座安全气囊\",\"valueitems\":[{\"specid\":25379,\"value\":\"主●\\u0026nbsp;/\\u0026nbsp;副●\"},{\"specid\":25381,\"value\":\"主●\\u0026nbsp;/\\u0026nbsp;副●\"},{\"specid\":25390,\"value\":\"主●\\u0026nbsp;/\\u0026nbsp;副●\"},{\"specid\":25382,\"value\":\"主●\\u0026nbsp;/\\u0026nbsp;副●\"}]},{\"name\":\"前/后排侧气囊\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后-\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后-\"},{\"specid\":25382,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后-\"}]},{\"name\":\"前/后排头部气囊(气帘)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"膝部气囊\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"胎压监测装置\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"零胎压继续行驶\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"安全带未系提示\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"ISOFIX儿童座椅接口\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"发动机电子防盗\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"车内中控锁\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"遥控钥匙\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"无钥匙启动系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"无钥匙进入系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"操控配置\",\"configitems\":[{\"name\":\"ABS防抱死\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"制动力分配(EBD/CBC等)\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"刹车辅助(EBA/BAS/BA等)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"牵引力控制(ASR/TCS/TRC等)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"车身稳定控制(ESC/ESP/DSC等)\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"上坡辅助\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"自动驻车\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"陡坡缓降\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"可变悬架\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"空气悬架\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"可变转向比\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前桥限滑差速器/差速锁\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"中央差速器锁止功能\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后桥限滑差速器/差速锁\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"外部配置\",\"configitems\":[{\"name\":\"电动天窗\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"全景天窗\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"运动外观套件\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"铝合金轮圈\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"○\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"○\"}]},{\"name\":\"电动吸合门\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"侧滑门\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"电动后备厢\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"感应后备厢\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"车顶行李架\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"内部配置\",\"configitems\":[{\"name\":\"真皮方向盘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"方向盘调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"上下调节\"},{\"specid\":25381,\"value\":\"上下调节\"},{\"specid\":25390,\"value\":\"上下调节\"},{\"specid\":25382,\"value\":\"上下调节\"}]},{\"name\":\"方向盘电动调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"多功能方向盘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"方向盘换挡\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"方向盘加热\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"方向盘记忆\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"定速巡航\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"前/后驻车雷达\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"前○\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25382,\"value\":\"前○\\u0026nbsp;/\\u0026nbsp;后●\"}]},{\"name\":\"倒车视频影像\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"行车电脑显示屏\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"全液晶仪表盘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"HUD抬头数字显示\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"座椅配置\",\"configitems\":[{\"name\":\"座椅材质\",\"valueitems\":[{\"specid\":25379,\"value\":\"织物\"},{\"specid\":25381,\"value\":\"织物\"},{\"specid\":25390,\"value\":\"皮/织物混搭\"},{\"specid\":25382,\"value\":\"织物\"}]},{\"name\":\"运动风格座椅\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"座椅高低调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"腰部支撑调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"肩部支撑调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"主/副驾驶座电动调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"第二排靠背角度调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"第二排座椅移动\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排座椅电动调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"电动座椅记忆\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前/后排座椅加热\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前/后排座椅通风\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前/后排座椅按摩\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"第三排座椅\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排座椅放倒方式\",\"valueitems\":[{\"specid\":25379,\"value\":\"整体放倒\"},{\"specid\":25381,\"value\":\"比例放倒\"},{\"specid\":25390,\"value\":\"比例放倒\"},{\"specid\":25382,\"value\":\"比例放倒\"}]},{\"name\":\"前/后中央扶手\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排杯架\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]}]},{\"name\":\"多媒体配置\",\"configitems\":[{\"name\":\"GPS导航系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"定位互动服务\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"中控台彩色大屏\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"蓝牙/车载电话\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"车载电视\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排液晶屏\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"220V/230V电源\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"外接音源接口\",\"valueitems\":[{\"specid\":25379,\"value\":\"USB+AUX+SD卡插槽\"},{\"specid\":25381,\"value\":\"USB+AUX+SD卡插槽\"},{\"specid\":25390,\"value\":\"USB+AUX+SD卡插槽\"},{\"specid\":25382,\"value\":\"USB+AUX+SD卡插槽\"}]},{\"name\":\"CD支持MP3/WMA\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"多媒体系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"单碟CD\"},{\"specid\":25390,\"value\":\"单碟CD\"},{\"specid\":25382,\"value\":\"单碟CD\"}]},{\"name\":\"扬声器品牌\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"扬声器数量\",\"valueitems\":[{\"specid\":25379,\"value\":\"4-5喇叭\"},{\"specid\":25381,\"value\":\"6-7喇叭\"},{\"specid\":25390,\"value\":\"6-7喇叭\"},{\"specid\":25382,\"value\":\"6-7喇叭\"}]}]},{\"name\":\"灯光配置\",\"configitems\":[{\"name\":\"近光灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"卤素\"},{\"specid\":25381,\"value\":\"卤素\"},{\"specid\":25390,\"value\":\"卤素(选装LED)\"},{\"specid\":25382,\"value\":\"卤素\"}]},{\"name\":\"远光灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"卤素\"},{\"specid\":25381,\"value\":\"卤素\"},{\"specid\":25390,\"value\":\"卤素(选装LED)\"},{\"specid\":25382,\"value\":\"卤素\"}]},{\"name\":\"日间行车灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"○\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"自适应远近光\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"自动头灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"转向辅助灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"转向头灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"前雾灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"大灯高度可调\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"大灯清洗装置\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"○\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"车内氛围灯\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"玻璃/后视镜\",\"configitems\":[{\"name\":\"前/后电动车窗\",\"valueitems\":[{\"specid\":25379,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25381,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25390,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"},{\"specid\":25382,\"value\":\"前●\\u0026nbsp;/\\u0026nbsp;后●\"}]},{\"name\":\"车窗防夹手功能\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"防紫外线/隔热玻璃\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"后视镜电动调节\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"后视镜加热\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"内/外后视镜自动防眩目\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后视镜电动折叠\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后视镜记忆\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后风挡遮阳帘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排侧遮阳帘\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后排侧隐私玻璃\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"遮阳板化妆镜\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"后雨刷\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"感应雨刷\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"空调/冰箱\",\"configitems\":[{\"name\":\"空调控制方式\",\"valueitems\":[{\"specid\":25379,\"value\":\"手动●\"},{\"specid\":25381,\"value\":\"手动●\"},{\"specid\":25390,\"value\":\"自动●\"},{\"specid\":25382,\"value\":\"手动●\"}]},{\"name\":\"后排独立空调\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"后座出风口\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"温度分区控制\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"车内空气调节/花粉过滤\",\"valueitems\":[{\"specid\":25379,\"value\":\"●\"},{\"specid\":25381,\"value\":\"●\"},{\"specid\":25390,\"value\":\"●\"},{\"specid\":25382,\"value\":\"●\"}]},{\"name\":\"车载冰箱\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]},{\"name\":\"高科技配置\",\"configitems\":[{\"name\":\"自动泊车入位\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"发动机启停技术\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"并线辅助\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"车道偏离预警系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"主动刹车/主动安全系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"整体主动转向系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"夜视系统\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"中控液晶屏分屏显示\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"自适应巡航\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]},{\"name\":\"全景摄像头\",\"valueitems\":[{\"specid\":25379,\"value\":\"-\"},{\"specid\":25381,\"value\":\"-\"},{\"specid\":25390,\"value\":\"-\"},{\"specid\":25382,\"value\":\"-\"}]}]}],\"search\":\"\\u003cdl\\u003e\\u003cdt\\u003e年\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e代\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e款：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e2016款\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e发\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e动\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e机：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e1.4L\\u003c/span\\u003e\\u003cspan class=\\\"item\\\"\\u003e1.4T\\u003c/span\\u003e\\u003cspan class=\\\"item\\\"\\u003e1.6L\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e车身结构：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e两厢车\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e座\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e位\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e数：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e5座\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e变\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e速\\u003cem class=\\\"halfword\\\"\\u003e\\u003c/em\\u003e箱：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e手动\\u003c/span\\u003e\\u003cspan class=\\\"item\\\"\\u003e双离合\\u003c/span\\u003e\\u003cspan class=\\\"item\\\"\\u003e自动\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e驱动方式：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e两驱\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\\u003cdl\\u003e\\u003cdt\\u003e能\\u003cem class=\\\"twoword\\\"\\u003e\\u003c/em\\u003e源：\\u003c/dt\\u003e\\u003cdd\\u003e\\u003cspan class=\\\"item\\\"\\u003e汽油\\u003c/span\\u003e\\u003c/dd\\u003e\\u003c/dl\\u003e\"}";
}
