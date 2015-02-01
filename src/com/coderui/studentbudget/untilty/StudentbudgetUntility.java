package com.coderui.studentbudget.untilty;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.coderui.studentbudget.db.Budget;
import com.coderui.studentbudget.db.MyDbHelper;

import android.database.Cursor;

public class StudentbudgetUntility{
	public static final String EXPENSESTABLE = "EXPENSES";
	public static final String INCOMETABLE = "INCOME";
	public static final String INCOME_CATEGORY_TABLE = "INCOME_CATEGORY";
	public static final String ITEM_TABLE = "ITEM";
	public static final String EXPENSES_CATEGORY_TABLE = "EXPENSES_CATEGORY";
	public static final String ACCOUNT_TABLE = "ACCOUNT";
	public static final String TRANSFERTABLE="TRANSFER";
	public static final String PASSWORD="mrbill";
	public static final int EXPENSES=0;//֧��
	public static final int INCOME=1;//����
	public static final int TRANSFER=2;//ת��
	public static boolean isFinished=false;
	private static String dateFrom = "yyyy/MM/dd";
	private static Calendar c =Calendar.getInstance();

	public static String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFrom);
		Date curentDate = new Date(System.currentTimeMillis());
		return sdf.format(curentDate);
	}
	
	public static int getYear(){
		int year=c.get(Calendar.YEAR);
		return year;
	}
	public static int getMonth(){
		int month=c.get(Calendar.MONTH);
		return month+1;
	}
	public static int getdate(){
		int date=c.get(Calendar.DAY_OF_MONTH);
		return date;
	}
	//�������ڡ�yy/MM/dd����ʽ�����ڻ�ȡ���� 
	public static String getWeekdays(String strdate) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Date date = sdf.parse(strdate);
		String[] weekDays = {"������", "����һ", "���ڶ�", "������", "������", "������", "������"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
	}
	
	//���ÿ�µ�����
	public static int getDays(int month,int cur_month,int cur_year){
		int days=0;
		if(month==cur_month){
			days= StudentbudgetUntility.getdate();
		}else{
			if(month==1||month==3||month==5||month==7||month==8||month==10||month==12){
				days=31;
			}
			if(month==4||month==6||month==9||month==11){
				days=30;
			}
			if(month==2){
				if(((cur_year%100==0)&&(cur_year%400==0))||((cur_year%100!=0)&&(cur_year%4==0))){
					days=29;
				}else{
					days=28;
				}
			}
		}
		return days;
	}
	//�������ݿ��е�budget
	public static void replaceAmount(String table,int budget_categoey,MyDbHelper dbHelper,Budget budget,String budgetId){
		Cursor cursor=dbHelper.select(table, budgetId);
		if(cursor.moveToFirst()){
			Double oldAmount=cursor.getDouble(cursor.getColumnIndex("BUDGET"));
			budget.setOld_amount(oldAmount);
		}
		cursor.close();
		dbHelper.update(table, budget,budget_categoey);
	}
}
