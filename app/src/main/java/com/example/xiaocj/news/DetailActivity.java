package com.example.xiaocj.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;


public class DetailActivity extends AppCompatActivity {
    static public Context context;

    private WebView webView = null;
    private Toolbar toolbar = null;
    private TextView textView = null;

    private Item showItem = null;
    private MenuItem likeItem = null;
    private int liked = 0;

    class MyWebClient extends WebViewClient{
        public MyWebClient(){

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Log.d("load url", url);
            // if (url.equals(curentUrl)) {
            //     view.loadUrl(url);
            //}
            return false;
        }

        @Override
        public void  onPageStarted(WebView view, String url, Bitmap favicon) {
            Toast.makeText(DetailActivity.this, "loading", Toast.LENGTH_SHORT);
            //设定加载开始的操作
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Toast.makeText(DetailActivity.this, "really", Toast.LENGTH_SHORT);
        setContentView(R.layout.webview);

        context = this;

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            showItem = new Item(extras.getString("object"));
            OperateDataBase.getInstance().updateViewed(showItem.getNewid());
            Log.d("show detail", "" + showItem.getImgUrl());
            initWebView(Item.html(showItem));
            initTextView();
        }
        liked = extras.getInt("like");
        ;

        // String url = intent.getStringExtra("url");
        initToolBar();

        Toast.makeText(DetailActivity.this, "gg", Toast.LENGTH_SHORT);
    }

    private void initWebView(String html){
        webView = (WebView)findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();

        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // settings.setUseWideViewPort(true);//设置webview推荐使用的窗口
        settings.setLoadWithOverviewMode(true);//设置webview加载的页面的模式
        settings.setDisplayZoomControls(false);//隐藏webview缩放按钮
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);//适应屏幕
        //settings.setLoadsImagesAutomatically(true);//自动加载图片
        settings.setDefaultTextEncodingName("utf-8");
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setAppCacheEnabled(true);

        webView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
        // webView.loadData(html, null, "UTF-8");
        webView.setWebViewClient(new MyWebClient());
            /*
            String html = extras.getString("description");
            WebView webView = (WebView)findViewById(R.id.webview);
            webView.loadData(html, "html", "utf-8");
            */
    }

    private void initTextView(){
        textView = (TextView)findViewById(R.id.textTitle);
        textView.setText(showItem.getTitle());
    }

    private void initToolBar(){
        Log.d("toolbar", "init");
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        //toolbar.inflateMenu(R.menu.toolbar0);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar0, menu);
        if (liked == 1)
            menu.findItem(R.id.like).setIcon(R.mipmap.like);
        else menu.findItem(R.id.like).setIcon(R.mipmap.unlike);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.like:
                Log.d("click", "item 1 like it");
                likeTheNews(item);
                break;
            case R.id.forward:
                Log.d("click", "share the news to someone else");
                share();
                break;
        }
        return true;
    }

    private void likeTheNews(MenuItem item){
        Item tmp = null;

        if (liked == 1){
            item.setIcon(R.mipmap.unlike);
            tmp = new Item(showItem.getTitle(), showItem.getUrl(), showItem.getNewid(), showItem.getImgUrl());
        }
        else {
            item.setIcon(R.mipmap.like);
            tmp = showItem;
        }
        liked = 1 - liked;

        final Item passItem = tmp;
        new Thread(new Runnable() {
            @Override
            public void run() {
                OperateDataBase.getInstance().updateDescription(passItem);
            }
        }).start();
    }

    private void share(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String imgUrl = showItem.getImgUrl();
        String content = showItem.getShareContent();
        if(imgUrl != null){
            //uri 是图片的地址
            shareIntent.putExtra(Intent.EXTRA_STREAM, imgUrl);
            shareIntent.setType("image/*");
            //当用户选择短信时使用sms_body取得文字
            shareIntent.putExtra("sms_body", content);
            Log.d("getImgUrl", imgUrl);
        }else{
            shareIntent.setType("text/plain");
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        //自定义选择框的标题
        //startActivity(Intent.createChooser(shareIntent, "邀请好友"));
        //系统默认标题
        startActivity(shareIntent);
    }
}
