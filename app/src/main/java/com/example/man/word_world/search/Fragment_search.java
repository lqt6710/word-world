package com.example.man.word_world.search;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.man.word_world.R;
import com.example.man.word_world.database.DBManager;
import com.example.man.word_world.search.model.Bean;
import com.example.man.word_world.search.util.CommonAdapter;
import com.example.man.word_world.search.util.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by man on 2016/11/27.
 */
public class Fragment_search extends Fragment implements View.OnClickListener {

    /**
     * 输入框
     */
    private EditText etInput;

    /**
     * 删除键
     */
    private ImageView ivDelete;

    /**
     * 返回按钮
     */
    private Button btnBack;

    /**
     * 搜索结果列表view
     */
    private ListView lvResults;

    /**
     * 热搜框列表adapter
     */
    private CommonAdapter<Bean> historyAdapter;

    /**
     * 自动补全列表adapter
     */
    private CommonAdapter<Bean> autoCompleteAdapter;

    /**
     * 搜索结果列表adapter
     */
    private CommonAdapter<Bean> resultAdapter;

    private List<Bean> dbData;

    /**
     * 热搜版数据
     */
    private List<Bean> historyData;

    /**
     * 搜索过程中自动补全数据
     */
    private List<Bean> autoCompleteData;

    /**
     * 搜索结果的数据
     */
    private List<Bean> resultData;

    /**
     * 默认提示框显示项的个数
     */
    private static int DEFAULT_AUTOCOMPLETE_SIZE = 10;

    /**
     * 提示框显示项的个数
     */
    private static int historySize;

    private DBManager dbManager;

    public Fragment_search(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_layout,container,false);
        initData();
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        initViews();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //创建、访问数据库
        dbManager =new DBManager(getActivity());
        //从数据库获取数据
        getWordsData();
        //初始化搜索历史数据
        getHistoryData();
        //初始化自动补全数据
        getAutoCompleteData(null);
        //初始化搜索结果数据
        getResultData(null);
    }

    /**
     * 获取单词 数据
     */
    private void getWordsData() {
        Cursor cursor=dbManager.getWordsData();
        dbData=new ArrayList<>();
        while (cursor.moveToNext()){
            Bean bean=new Bean();
            bean.setWord(cursor.getString(cursor.getColumnIndex("english")));
            dbData.add(bean);
        }
        cursor.close();
    }

    /**
     * 获取搜索历史data 和adapter
     */
    private void getHistoryData() {
        Cursor cursor=dbManager.getHistoryData();
        historySize=cursor.getCount();
        if (historyData == null){
            historyData = new ArrayList<>(historySize);
        }else{
            historyData.clear();
        }
        while (cursor.moveToNext()){
            Bean bean=new Bean();
            bean.setWord(cursor.getString(cursor.getColumnIndex("spelling")));
            historyData.add(bean);
        }
        if (historyAdapter == null){
            historyAdapter= new CommonAdapter<Bean>(getActivity(), android.R.layout.simple_list_item_1, historyData) {
                @Override
                public void convert(ViewHolder holder, int position) {
                    holder.setText(android.R.id.text1 , mData.get(position).getWord());
                }
            };
        }else{
            historyAdapter.notifyDataSetChanged();
        }
        cursor.close();
    }

    /**
     * 获取自动补全data 和adapter
     */
    private void getAutoCompleteData(String text) {
        if (autoCompleteData == null) {
            //初始化
            autoCompleteData = new ArrayList<>(DEFAULT_AUTOCOMPLETE_SIZE);
        } else {
            // 根据text 获取auto data
            autoCompleteData.clear();
            for (int i = 0, count = 0; i < dbData.size()
                    && count < DEFAULT_AUTOCOMPLETE_SIZE; i++) {
                Bean bean=dbData.get(i);
                if (bean.getWord().startsWith(text.trim())) {
                    autoCompleteData.add(bean);
                    count++;
                }
            }
        }
        if (autoCompleteAdapter == null) {
            autoCompleteAdapter = new CommonAdapter<Bean>(getActivity(), android.R.layout.simple_list_item_1, autoCompleteData) {
                @Override
                public void convert(ViewHolder holder, int position) {
                    holder.setText(android.R.id.text1 , mData.get(position).getWord());
                }
            };
        } else {
            autoCompleteAdapter.notifyDataSetChanged();
        }    }

    /**
     * 获取搜索结果data和adapter
     */
    private void getResultData(String text) {
        if (resultData == null) {
            // 初始化
            resultData = new ArrayList<>();
        } else {
            resultData.clear();
            for (int i = 0; i < dbData.size(); i++) {
                Bean bean=dbData.get(i);
                if (bean.getWord().contains(text.trim())) {
                    resultData.add(bean);
                }
            }
        }
        if (resultAdapter == null) {
            resultAdapter = new CommonAdapter<Bean>(getActivity(), android.R.layout.simple_list_item_1, resultData) {
                @Override
                public void convert(ViewHolder holder, int position) {
                    holder.setText(android.R.id.text1 , mData.get(position).getWord());
                }
            };
        } else {
            resultAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //注册控件
        etInput=(EditText)getActivity().findViewById(R.id.search_et_input);
        ivDelete=(ImageView)getActivity().findViewById(R.id.search_iv_delete);
        btnBack=(Button)getActivity().findViewById(R.id.search_btn_back);
        lvResults = (ListView) getActivity().findViewById(R.id.main_lv_search_results);
        lvResults.setAdapter(historyAdapter);

        //设置adapter
        //lvResults.setAdapter(historyAdapter);

        //设置点击事件
        ivDelete.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        //设置监听
        etInput.addTextChangedListener(new EditChangedListener());
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    autoCompleteSearching(etInput.getText().toString().trim());
                }
                return true;
            }
        });

        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Toast.makeText(getActivity(), position + "", Toast.LENGTH_SHORT).show();
                if (lvResults.getAdapter() == historyAdapter){
                    etInput.setText(historyData.get(position).getWord());
                    historySearching(etInput.getText().toString());
                }else if (lvResults.getAdapter() == autoCompleteAdapter){
                    etInput.setText(autoCompleteData.get(position).getWord());
                    autoCompleteSearching(etInput.getText().toString());
                }else {
                    etInput.setText(resultData.get(position).getWord());
                    historySearching(etInput.getText().toString());//因为只是传递进去的数据源不同，所以就共用一个方法了
                }
            }
        });
    }

    /**
     * 通知监听者 进行搜索操作
     * @param text
     */
    private void autoCompleteSearching(String text){
        //隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        //将搜索的单词添加到搜索历史表中
        onRefreshHistoryData(text);

        //搜索
        onSearch(etInput.getText().toString());
    }

    private void historySearching(String text){
        //将搜索的单词添加到搜索历史表中，并更新historyData
        onRefreshHistoryData(text);

        //搜索
        onSearch(etInput.getText().toString());
    }

    private void onRefreshHistoryData(String text){
        dbManager.refreshHistoryData(text);
        getHistoryData();
    }

    private void onSearch(String text) {
        //更新result数据
        getResultData(text);
        //lvResults.setVisibility(View.VISIBLE);
        //第一次获取结果 还未配置适配器
        if (lvResults.getAdapter() == null) {
            //获取搜索数据 设置适配器
            lvResults.setAdapter(resultAdapter);
        } else {
            //更新搜索数据
            resultAdapter.notifyDataSetChanged();
        }
        Intent intent=new Intent(getActivity(),DictionaryActivity.class);
        startActivity(intent);
        //Toast.makeText(getActivity(), "完成搜索", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_iv_delete:
                etInput.setText("");
                lvResults.setAdapter(historyAdapter);
                ivDelete.setVisibility(View.GONE);
                break;
            case R.id.search_btn_back:
                break;
        }
    }

    private class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (!"".equals(charSequence.toString())) {
                ivDelete.setVisibility(View.VISIBLE);
                //更新autoComplete数据
                onRefreshAutoComplete(charSequence + "");
            } else {
                ivDelete.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private void onRefreshAutoComplete(String text) {
        //更新数据
        lvResults.setAdapter(autoCompleteAdapter);
        getAutoCompleteData(text);
    }
}
