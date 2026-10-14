package com.jelibon.evdeantreman;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
  LinearLayout root, content; TextView clock; Handler handler=new Handler();
  long start=0; boolean running=false;
  final String[][] names={
    {"Dambıl Bench Press","Eğimli Dambıl Press","Dambıl Omuz Press","Yana Açış","EZ Bar Triceps Extension"},
    {"Barbell Row","Tek Kol Dambıl Row","EZ Bar Curl","Hammer Curl","Arka Omuz Açış"},
    {"Goblet Squat","Barbell Romanian Deadlift","Dambıl Lunge","Calf Raise","Glute Bridge"}
  };
  final String[] days={"PUSH","PULL","LEGS"};
  int selected=0;
  android.content.SharedPreferences prefs;
  public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("training",0);showHome();}
  TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(235,240,255));t.setPadding(14,13,14,13);return t;}
  void init(String title){ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,25,18,35);root.setBackgroundColor(Color.rgb(13,19,33));scroll.addView(root);setContentView(scroll);TextView h=text(title,27);h.setTextColor(0xff74e0c4);root.addView(h);}
  Button button(String label, Runnable run){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setOnClickListener(v->run.run());root.addView(b);return b;}
  void showHome(){init("EVDE ANTRENMAN");root.addView(text("68 kg ağırlık • Dambıl • Düz/EZ bar • Bench",14));for(int i=0;i<3;i++){final int d=i;button(days[i]+"  •  "+names[i].length+" hareket",()->showDay(d));}button("Antrenman geçmişi",()->showHistory());button("Vücut ağırlığı takibi",()->showWeight());root.addView(text("Hareket anlatımları için internet üzerinden video araması açılır. GIF gösterimleri sonraki sürüm için planlanmıştır.",12));}
  void showDay(int d){selected=d;init(days[d]+" ANTRENMANI");button("← Ana sayfa",()->showHome());clock=text("Süre 00:00",19);root.addView(clock);button("Başlat / Duraklat",()->{running=!running;if(running){start=System.currentTimeMillis();tick();}});button("60 sn dinlenme sayacı",()->rest(60));for(int i=0;i<names[d].length;i++){final int idx=i;String name=names[d][i];root.addView(text("● "+name,19));button("Hareketi göster ↗",()->{String q=android.net.Uri.encode(name+" exercise tutorial");startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.youtube.com/results?search_query="+q)));});String prefix=d+"_"+idx;int n=prefs.getInt(prefix+"_count",3);for(int s=0;s<n;s++){final int set=s;LinearLayout line=new LinearLayout(this);line.setOrientation(0);TextView tag=text((s+1)+". set",14);line.addView(tag);EditText kg=new EditText(this);kg.setSingleLine(true);kg.setHint("kg");kg.setText(prefs.getString(prefix+"_"+s+"_kg",""));kg.setInputType(8194);kg.setTextColor(-1);line.addView(kg,new LinearLayout.LayoutParams(0,-2,1));EditText reps=new EditText(this);reps.setSingleLine(true);reps.setHint("tekrar");reps.setText(prefs.getString(prefix+"_"+s+"_reps",""));reps.setInputType(2);reps.setTextColor(-1);line.addView(reps,new LinearLayout.LayoutParams(0,-2,1));root.addView(line);kg.addTextChangedListener(watch(v->prefs.edit().putString(prefix+"_"+set+"_kg",v).apply()));reps.addTextChangedListener(watch(v->prefs.edit().putString(prefix+"_"+set+"_reps",v).apply()));}button("+ Set ekle",()->{prefs.edit().putInt(prefix+"_count",prefs.getInt(prefix+"_count",3)+1).apply();showDay(d);});button("− Set sil",()->{int c=prefs.getInt(prefix+"_count",3);if(c>1){prefs.edit().putInt(prefix+"_count",c-1).apply();showDay(d);}});}button("ANTRENMANI KAYDET",()->{String date=new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault()).format(new Date());String history=prefs.getString("history","");prefs.edit().putString("history",date+" — "+days[d]+"\n"+history).apply();running=false;Toast.makeText(this,"Antrenman kaydedildi",0).show();showHome();});}
  interface OnValue {void value(String s);}
  android.text.TextWatcher watch(OnValue fn){return new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){fn.value(s.toString());}public void afterTextChanged(android.text.Editable e){}};}
  void tick(){if(!running)return;long sec=(System.currentTimeMillis()-start)/1000;if(clock!=null)clock.setText(String.format(Locale.getDefault(),"Süre %02d:%02d",sec/60,sec%60));handler.postDelayed(()->tick(),1000);}
  void rest(int seconds){final TextView t=text("",22);root.addView(t);final long end=System.currentTimeMillis()+seconds*1000;Runnable[] task=new Runnable[1];task[0]=()->{long left=Math.max(0,(end-System.currentTimeMillis()+999)/1000);t.setText("Dinlenme: "+left+" sn");if(left>0)handler.postDelayed(task[0],500);else Toast.makeText(this,"Dinlenme tamamlandı!",0).show();};task[0].run();}
  void showHistory(){init("ANTRENMAN GEÇMİŞİ");button("← Ana sayfa",()->showHome());root.addView(text(prefs.getString("history","Henüz kayıt yok."),17));}
  void showWeight(){init("VÜCUT AĞIRLIĞI");button("← Ana sayfa",()->showHome());EditText input=new EditText(this);input.setHint("Kilonuz (kg)");input.setTextColor(-1);input.setInputType(8194);root.addView(input);button("Kaydet",()->{String s=input.getText().toString();if(!s.isEmpty()){String date=new java.text.SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(new Date());prefs.edit().putString("weights",date+" : "+s+" kg\n"+prefs.getString("weights","")).apply();showWeight();}});root.addView(text(prefs.getString("weights","Henüz kayıt yok."),17));}
}
