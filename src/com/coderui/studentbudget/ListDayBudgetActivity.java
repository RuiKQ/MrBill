package com.coderui.studentbudget;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coderui.studentbudget.db.MyDbHelper;
import com.coderui.studentbudget.untilty.StudentbudgetUntility;

public class ListDayBudgetActivity extends Activity implements OnClickListener{
	private MyDbHelper mDb;
	private int month,tempmonth;
	private int year;
	private int cur_month;
	private Double amountSum;
	private TextView title_month;
	private ImageView previous_day,back_home,next_day;
	private LinearLayout search_bar;
	private ExpandableListView expandableListView;
	private List<Map<String, Object>> parentLsit;
	private List<List<Map<String, Object>>> childList;
	private String[] expense_explanation;
	private Integer[] expense_images;
	private Cursor cursor;
	private MyExpandableListAdapter adapter;
	private final int REQUESTCODE=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_day_budget_view);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		month = bundle.getInt("month");
		year = bundle.getInt("year");
		tempmonth=month;
		getInstance();
		getListData(month,cur_month);
		// ���б����
		expandableListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				dealwithbudget(parent,v,groupPosition,childPosition,id);
				return false;
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	private void getInstance() {
		mDb = SplashScreenActivity.db;
		cur_month = StudentbudgetUntility.getMonth();
		expense_explanation = this.getResources().getStringArray(
				R.array.EXPENSES_CATEGORY);
		expense_images = new Integer[] { R.drawable.repast,
				R.drawable.xiuxianyule, R.drawable.book, R.drawable.shsj,
				R.drawable.juhuijiaoyou, R.drawable.liwu,
				R.drawable.huafeiwangfei, R.drawable.transport,
				R.drawable.cloth, R.drawable.touzikuisun, R.drawable.yiyao,
				R.drawable.other };
		parentLsit = new ArrayList<Map<String, Object>>();
		childList = new ArrayList<List<Map<String, Object>>>();
		title_month = (TextView) findViewById(R.id.title_month);
		
//		search_bar = (LinearLayout) findViewById(R.id.search_bar);
//		search_bar.setOnClickListener(this);
		previous_day=(ImageView) findViewById(R.id.previous_day);
		previous_day.setOnClickListener(this);
		back_home=(ImageView) findViewById(R.id.backhome);
		back_home.setOnClickListener(this);
		next_day=(ImageView) findViewById(R.id.next_day);
		next_day.setOnClickListener(this);
		expandableListView = (ExpandableListView) findViewById(R.id.day_list);
		// ÿ��ֻչʾ��һ��group
				expandableListView
						.setOnGroupExpandListener(new OnGroupExpandListener() {
							@Override
							public void onGroupExpand(int groupPosition) {
								for (int i = 0; i < adapter.getGroupCount(); i++) {
									if (groupPosition != i) {
										expandableListView.collapseGroup(i);
									}
								}
							}
						});
	}
	//�õ������˵��ļ�¼
	private void getListData(int month,int cur_month) {
		parentLsit.clear();
		childList.clear();
		title_month.setText(String.valueOf(month));
		for (int i = StudentbudgetUntility.getDays(month, cur_month, year); i > 0; i--) {
			amountSum = 0.0;
			Map<String, Object> parentMap = new HashMap<String, Object>();
			String date = StudentbudgetUntility.getYear() + "/" + format(month) + "/"
					+ format(i);// ÿ�������
			cursor = mDb.query("select * from EXPENSES where DATE =?",
					new String[] { date });
			List<Map<String, Object>> child = new ArrayList<Map<String, Object>>();
			while (cursor.moveToNext()) {
				Map<String, Object> childMap = new HashMap<String, Object>();
				double amount = cursor.getDouble(cursor
						.getColumnIndex("AMOUNT"));// ���֧�����
				Integer id = cursor.getInt(cursor
						.getColumnIndex("EXPENSES_CATEGORY_ID"));// ���֧������ID
				childMap.put("image", expense_images[id - 1]);
				childMap.put("explanation", expense_explanation[id - 1]);
				childMap.put("amount", amount);
				child.add(childMap);
				amountSum += amount;
			}
			String strDate = year + "/" + format(month) + "/" + format(i);// ���String���͵ĵ�������
			childList.add(child);
			parentMap.put("day", i);
			parentMap.put("strDate", strDate);
			parentMap.put("amountSum", amountSum);
			parentLsit.add(parentMap);
		}
		cursor.close();
		adapter = new MyExpandableListAdapter(
				this, parentLsit, childList);
		expandableListView.setAdapter(adapter);
	}
	

	// ����Ի����е�����
	@SuppressWarnings("unchecked")
	private void dealwithbudget(View parent,View child,final int groupPosition, final int childPosition, long id) {
		Map<String,Object> groupmap=(Map<String,Object>)adapter.getGroup(groupPosition);
		Map<String,Object> childmap=(Map<String,Object>)adapter.getChild(groupPosition, childPosition);
		final String date=(String) groupmap.get("strDate");//�õ�����
		final String str_category=(String)childmap.get("explanation");
		final Double amount=(Double)childmap.get("amount");
		final Double amountAll=(Double)groupmap.get("amountSum");
		final AlertDialog dlg = new AlertDialog.Builder(
				ListDayBudgetActivity.this).create();
		dlg.show();
		Window window = dlg.getWindow();
		window.setContentView(R.layout.dialog_budgetlist_view);
		ImageButton edit_imb = (ImageButton) window
				.findViewById(R.id.edit_budget);// �༭��¼
		ImageButton delete = (ImageButton) window
				.findViewById(R.id.delete_budget);// ɾ����¼
		ImageButton cancle = (ImageButton) window
				.findViewById(R.id.close_dialog);// �رնԻ���
		//�༭�˵���¼
		edit_imb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int expense_category_id = 0;
				for (int i = 0; i < expense_explanation.length; i++) {
					if (str_category
							.equals(expense_explanation[i])) {
						expense_category_id = i + 1;
					}
				}
				Bundle bundle=new Bundle();
				bundle.putString("date",date);
				bundle.putInt("category", expense_category_id);
				bundle.putDouble("amount", amount);
				Intent intent=new Intent(ListDayBudgetActivity.this,EditBudget.class);
				intent.putExtras(bundle);
				ListDayBudgetActivity.this.startActivityForResult(intent, REQUESTCODE);
				dlg.dismiss();
			}
		});
		//ɾ���˵���¼
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog dialog = new AlertDialog.Builder(
						ListDayBudgetActivity.this)
						.setTitle("����")
						.setMessage("ȷ��Ҫɾ��������¼��")
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										int expense_category_id = 0;
										for (int i = 0; i < expense_explanation.length; i++) {
											if (str_category
													.equals(expense_explanation[i])) {
												expense_category_id = i + 1;
											}
										}
										try {
											mDb.execSQL(
													"DELETE FROM EXPENSES WHERE AMOUNT=? and EXPENSES_CATEGORY_ID=? and DATE=?",
													new Object[] {amount,expense_category_id,date});
											childList.get(groupPosition).remove(childPosition);//ˢ�����б�����
											parentLsit.get(groupPosition).put("amountSum",amountAll-amount);//�޸�group�б�����
											adapter.notifyDataSetChanged();//ˢ�µ�����
											Toast.makeText(ListDayBudgetActivity.this, "ɾ���ɹ�", Toast.LENGTH_SHORT).show();
										} catch (SQLException e) {
											e.printStackTrace();
										}
										dlg.dismiss();
									}
								})
						.setNegativeButton("ȡ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dlg.dismiss();
									}
								}).create();
				dialog.show();
			}
		});
		//ȡ���˵�
		cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.dismiss();// �رնԻ���
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REQUESTCODE){
			tempmonth=month;
			getListData(month,cur_month);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//		case R.id.search_bar:
//			//��������
//			
//			break;
		case R.id.backhome:
			Intent intent=new Intent(ListDayBudgetActivity.this,HomepagerActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.previous_day:
			if(tempmonth<=1){
				Toast.makeText(ListDayBudgetActivity.this, "����1��", Toast.LENGTH_SHORT).show();
			}else{
				tempmonth--;
				getListData(tempmonth,cur_month);
			}
			break;
		case R.id.next_day:
			if(tempmonth>=cur_month){
				Toast.makeText(ListDayBudgetActivity.this, "û����һ������!", Toast.LENGTH_SHORT).show();
			}else{
				tempmonth++;
				getListData(tempmonth,cur_month);
			}
			break;
		default:
			break;
		}
	}
	private String format(int x)  
    {  
      String s=""+x;  
      if(s.length()==1) s="0"+s;  
      return s;  
    }  
}
