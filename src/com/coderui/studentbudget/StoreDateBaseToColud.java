package com.coderui.studentbudget;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.oauth.BaiduOAuth;
import com.baidu.oauth.BaiduOAuth.BaiduOAuthResponse;
import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.baidu.pcs.BaiduPCSStatusListener;
import com.coderui.studentbudget.db.MyDbHelper;
import com.coderui.studentbudget.untilty.StudentbudgetUntility;

public class StoreDateBaseToColud extends Activity {
	final static private String TAG = "StoreDateBaseToColud";

	final static private String APP_KEY = "KbVb9DiUh8E64PVZmHQI4NDt";
	final static private String APP_ROOT = "/apps/�˵�����/";
	private MyDbHelper mDb;
	private Cursor cursor;
	private ListView list;
	private String mbOauth = null;
	private Button uploader_btn;
	private Button login_pcs;
	private Button downloader;
	private Button back;
	private TextView nobackup;
	private ArrayList<String> backupdata=new ArrayList<String>();
	private Handler mbUiThreadHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.back_up_to_net);
		mbUiThreadHandler = new Handler();
		mDb = SplashScreenActivity.db;
		list=(ListView) findViewById(R.id.list_backup);
		nobackup=(TextView)findViewById(R.id.nobackup);
		getBackupRecord();
		back=(Button) findViewById(R.id.backup_return_btn);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		login_pcs = (Button) findViewById(R.id.login_pcs);
		login_pcs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				lodinPcs();
			}
		});
		uploader_btn = (Button) findViewById(R.id.Uploader);
		uploader_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//��ɾ�����ϴ�
				if(!backupdata.isEmpty()){
					delete();
				}
				uploader();
			}
		});
		downloader=(Button) findViewById(R.id.Downloader);
		downloader.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				backupdatabase();
			}
		});
	}

	private void lodinPcs() {
		BaiduOAuth oauthClient = new BaiduOAuth();
		oauthClient.startOAuth(this, APP_KEY, new BaiduOAuth.OAuthListener() {
			@Override
			public void onException(String msg) {
				Toast.makeText(getApplicationContext(), "��½ʧ��" + msg,Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onComplete(BaiduOAuthResponse response) {
				if (null != response) {
					mbOauth = response.getAccessToken();//��ȡAccess_token
					Toast.makeText(getApplicationContext(), "��½�ɹ�!", Toast.LENGTH_SHORT).show();
					login_pcs.setText("�ѵ�¼");
					Log.v(TAG,"Token: " + mbOauth + "User name:"+ response.getUserName());
				}
			}
			@Override
			public void onCancel() {
				Toast.makeText(getApplicationContext(), "��ȡ���˵�½",Toast.LENGTH_SHORT).show();
			}
		});
	}
	//�ϴ��ļ����ٶ�����
	private void uploader() {
		if (null != mbOauth) {
			Thread workThread = new Thread(new Runnable() {
				public void run() {
					String tmpFile = "/data/data/com.coderui.studentbudget/databases/MyBudget.db";//���ݿ��ַ
					BaiduPCSClient api = new BaiduPCSClient();
					api.setAccessToken(mbOauth);
					final BaiduPCSActionInfo.PCSFileInfoResponse response = api.uploadFile(tmpFile, APP_ROOT + "/MyBudget.db",new BaiduPCSStatusListener() {
						@Override
						public void onProgress(long bytes,long total) {
							final long bs = bytes;
							final long tl = total;
							mbUiThreadHandler.post(new Runnable() {
								public void run() {
									//�����ļ��������
									Log.v(TAG, "�ܼ�: "+ tl+ "    �ѷ���:"+ bs);
									}
								});
							}
						@Override
						public long progressInterval() {
							return 1000;
							}
						});
					
					mbUiThreadHandler.post(new Runnable() {
						public void run() {
							if(response.status.errorCode==0){//0��ʾ�ɹ�
								Toast.makeText(getApplicationContext(), "���ݳɹ�", Toast.LENGTH_SHORT).show();
								String backrecord="MrBill/"+response.commonFileInfo.mTime+"/"+response.commonFileInfo.size;
								try {
									mDb.execSQL("INSERT INTO BACKUP(NAME) VALUES(?)", new Object[]{backrecord});
								} catch (SQLException e) {
									e.printStackTrace();
								}
								getBackupRecord();
		    				}else{
		    					Toast.makeText(getApplicationContext(), "����ʧ��,������", Toast.LENGTH_SHORT).show();
		    				}
						}
					});
				}
			});
			workThread.start();
		}
	}
	
	private void backupdatabase(){
		if(null!=mbOauth){
			Thread workThread=new Thread(new Runnable(){
				@Override
				public void run() {
					BaiduPCSClient api = new BaiduPCSClient();
		    		api.setAccessToken(mbOauth);
		    		String source = APP_ROOT + "/MyBudget.db";
		    		String target = "/data/data/com.coderui.studentbudget/databases/MyBudget.db";
		    		final BaiduPCSActionInfo.PCSSimplefiedResponse ret = api.downloadFileFromStream(source, target, new BaiduPCSStatusListener(){
						//����һ���ļ�����Ѿ��ж����ֽڱ�����
						//bytes - �Ѿ�������ֽ���
						//total - ���ж����ֽ�
		    			@Override
						public void onProgress(long bytes, long total) {
							final long bs = bytes;
							final long tl = total;
				    		mbUiThreadHandler.post(new Runnable(){
				    			public void run(){
				    				Log.v(TAG, "total: " + tl + "    downloaded:" + bs);
				    			}
				    		});		
						}
						//����ÿ�μ������ȵ�ʱ�������Ժ���Ϊ��λ
		    			@Override
						public long progressInterval(){
							return 500;
						}
		    		});
		    		mbUiThreadHandler.post(new Runnable(){
		    			public void run(){
		    				if(ret.errorCode==0){
		    					Toast.makeText(getApplicationContext(), "�ָ��ɹ�", Toast.LENGTH_SHORT).show();
		    				}else{
		    					Toast.makeText(getApplicationContext(), "�ָ�ʧ��,������", Toast.LENGTH_SHORT).show();
		    				}
		    			}
		    		});	
				}
			});
			workThread.start();
		}
	}
	
	private void delete(){
		if(null != mbOauth){

    		Thread workThread = new Thread(new Runnable(){
				public void run() {

		    		BaiduPCSClient api = new BaiduPCSClient();
		    		api.setAccessToken(mbOauth);
		    		final BaiduPCSActionInfo.PCSSimplefiedResponse ret = api.deleteFile(APP_ROOT + "/MyBudget.db");
		    		mbUiThreadHandler.post(new Runnable(){
		    			public void run(){
		    				Log.v(TAG,"Delete files:  " + ret.errorCode + "  " + ret.message);
		    			}
		    		});	
				}
			});
    		workThread.start();
    	}
	}
	private void getBackupRecord(){
		backupdata.clear();
		cursor=mDb.rawQuery("BACKUP");
		while(cursor.moveToNext()){
			String record=cursor.getString(cursor.getColumnIndex("NAME"));
			backupdata.add(record);
		}
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,backupdata);
		list.setAdapter(adapter);
		if(backupdata.isEmpty()){
			nobackup.setVisibility(View.VISIBLE);
		}else{
			nobackup.setVisibility(View.GONE);
		}
	}
}
