package com.abenezer.aritimarket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String API = "https://uuuiysoowojsqytgdnoo.supabase.co";
    private static final String KEY = "sb_publishable_klraPqTbuK72dLJCirpIxg_qlPL1522";
    private static final String PREFS = "ariti_session";
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ArrayList<JSONObject> products = new ArrayList<>();
    private LinearLayout root, content, productList;
    private EditText search, email, password, productName, price, category, seller, phone, description;
    private TextView message, accountLabel;
    private int languageIndex = 0; // 0 Amharic, 1 English, 2 Afaan Oromoo
    private String accessToken = "";
    private String userId = "";
    private Uri selectedImageUri = null;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        accessToken = p.getString("access_token", "");
        userId = p.getString("user_id", "");
        languageIndex = p.getInt("language_index", 0);
        buildScreen();
        loadProducts();
    }

    private String tr(String am, String en) {
        if (languageIndex == 0) return am;
        if (languageIndex == 1) return en;
        switch (en) {
            case "Buy and sell with ease.": return "Salphaatti bitadhaa, gurguradhaa.";
            case "Discover products, post listings and contact sellers.": return "Oomishaalee barbaadaa, beeksisa maxxansaa, gurgurtoota qunnamaa.";
            case "Search products…": return "Oomisha barbaadi…";
            case "Browsing as guest": return "Akka keessummaatti daawwachaa jirta";
            case "Signed in": return "Seenteetta";
            case "Account": return "Herrega";
            case "Password": return "Jecha iccitii";
            case "Sign in": return "Seeni";
            case "Sign up": return "Galmaa'i";
            case "Sign out": return "Ba'i";
            case "Post a product": return "Oomisha maxxansi";
            case "Product name": return "Maqaa oomishaa";
            case "Price (ETB)": return "Gatii (ETB)";
            case "Category": return "Ramaddii";
            case "Seller name": return "Maqaa gurguraa";
            case "Phone number": return "Lakkoofsa bilbilaa";
            case "Product description": return "Ibsa oomishaa";
            case "Publish listing": return "Beeksisa maxxansi";
            case "Latest products": return "Oomishaalee haaraa";
            case "Enter email and a password of at least 6 characters.": return "Imeelii fi jecha iccitii qubee 6 ol qabu galchi.";
            case "Sign-up submitted. Check your email to confirm.": return "Galmeen ergameera. Imeelii kee mirkaneessi.";
            case "No session returned. Confirm your email, then sign in.": return "Seensaan hin milkoofne. Imeelii kee mirkaneessi, achiis seeni.";
            case "Signed in successfully.": return "Milkaa'inaan seenteetta.";
            case "Please sign in before posting.": return "Oomisha maxxansuu dura seeni.";
            case "Complete product, seller and phone fields.": return "Maqaa oomishaa, gurguraa fi lakkoofsa bilbilaa guuti.";
            case "Product published.": return "Oomishni maxxanfameera.";
            case "Enter a valid price.": return "Gatii sirrii galchi.";
            case "Could not load products. Check your internet connection.": return "Oomishaaleen hin fe'amne. Intarneetii kee mirkaneessi.";
            case "Contact seller on WhatsApp": return "WhatsApp irratti gurguraa qunnami";
            case "No products found.": return "Oomishni hin argamne.";
            default: return en;
        }
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView v = new TextView(this);
        v.setText(value); v.setTextSize(size); v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        v.setPadding(dp(2), dp(5), dp(2), dp(5));
        return v;
    }
    private EditText field(String hint, int inputType) {
        EditText e = new EditText(this);
        e.setSingleLine(inputType != 0);
        e.setHint(hint); e.setTextSize(15); e.setTextColor(Color.rgb(24,35,54));
        e.setHintTextColor(Color.GRAY); e.setInputType(inputType);
        e.setPadding(dp(12), dp(8), dp(12), dp(8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(48));
        lp.bottomMargin = dp(8); e.setLayoutParams(lp);
        return e;
    }
    private Button button(String label, boolean primary) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false);
        b.setTextColor(primary ? Color.WHITE : Color.rgb(25,50,90));
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                primary ? Color.rgb(37,99,235) : Color.rgb(232,239,250)));
        return b;
    }
    private void buildScreen() {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245,247,251));
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(12), dp(12), dp(12));
        header.setBackgroundColor(Color.rgb(16,29,53));
        TextView brand = text("ARITI MARKET", 21, true, Color.WHITE);
        header.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        Button language = button(languageIndex == 0 ? "አማርኛ ▾" : languageIndex == 1 ? "English ▾" : "Afaan Oromoo ▾", false);
        header.addView(language); language.setOnClickListener(v -> {
            languageIndex = (languageIndex + 1) % 3;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt("language_index", languageIndex).apply();
            buildScreen(); loadProducts();
        });
        root.addView(header);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(24));
        scroll.addView(content); root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        TextView hero = text(tr("በቀላሉ ይግዙ፣ ይሽጡ።","Buy and sell with ease."), 24, true, Color.WHITE);
        hero.setPadding(dp(18),dp(22),dp(18),dp(22));
        hero.setBackgroundColor(Color.rgb(24,70,137)); content.addView(hero);
        TextView sub = text(tr("ምርቶችን ይፈልጉ፣ ያስታውቁ፣ ከሻጮች ጋር ይገናኙ።","Discover products, post listings and contact sellers."),14,false,Color.rgb(76,91,113));
        content.addView(sub);
        search = field(tr("ምርት ፈልግ…","Search products…"), 0); content.addView(search);
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){ renderProducts(); }
            public void afterTextChanged(android.text.Editable e){}
        });
        accountLabel = text("", 16, true, Color.rgb(25,50,90)); content.addView(accountLabel);
        buildAuthAndPost();
        message = text("", 13, false, Color.rgb(180,55,45)); content.addView(message);
        TextView section = text(tr("የቅርብ ጊዜ ምርቶች","Latest products"),20,true,Color.rgb(20,35,58));
        content.addView(section);
        productList = new LinearLayout(this); productList.setOrientation(LinearLayout.VERTICAL);
        content.addView(productList);
        accountLabel.setText(accessToken.isEmpty() ? tr("እንግዳ ተጠቃሚ","Browsing as guest") : tr("ገብተዋል","Signed in"));
        setContentView(root);
    }
    private void buildAuthAndPost() {
        TextView authTitle = text(tr("መለያ","Account"),18,true,Color.rgb(20,35,58)); content.addView(authTitle);
        email = field("Email", 33); password = field(tr("የይለፍ ቃል","Password"), 129); content.addView(email); content.addView(password);
        LinearLayout authButtons = new LinearLayout(this); authButtons.setOrientation(LinearLayout.HORIZONTAL);
        Button signIn = button(tr("ግባ","Sign in"),true);
        Button signUp = button(tr("ተመዝገብ","Sign up"),false);
        authButtons.addView(signIn,new LinearLayout.LayoutParams(0,-2,1));
        authButtons.addView(signUp,new LinearLayout.LayoutParams(0,-2,1)); content.addView(authButtons);
        signIn.setOnClickListener(v -> authenticate(false)); signUp.setOnClickListener(v -> authenticate(true));
        Button signOut = button(tr("ውጣ","Sign out"),false);
        signOut.setOnClickListener(v -> { accessToken="";userId="";getSharedPreferences(PREFS,MODE_PRIVATE).edit().clear().apply();buildScreen();loadProducts(); });
        content.addView(signOut);
        TextView postTitle = text(tr("ምርት ይለጥፉ","Post a product"),18,true,Color.rgb(20,35,58)); content.addView(postTitle);
        productName = field(tr("የምርት ስም","Product name"),0);
        price = field(tr("ዋጋ (ETB)","Price (ETB)"),8194);
        category = field(tr("ምድብ","Category"),0);
        seller = field(tr("የሻጭ ስም","Seller name"),0);
        phone = field(tr("ስልክ ቁጥር","Phone number"),3);
        description = field(tr("የምርት መግለጫ","Product description"),0);
        for(EditText e:new EditText[]{productName,price,category,seller,phone,description}) content.addView(e);
        Button chooseImage = button(tr("ፎቶ ምረጥ","Choose product photo"),false); content.addView(chooseImage);
        chooseImage.setOnClickListener(v -> { Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT); pick.setType("image/*"); pick.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(pick, 4102); });
        Button publish = button(tr("አሳትም","Publish listing"),true); content.addView(publish);
        publish.setOnClickListener(v -> publishProduct());
    }
    private void authenticate(boolean signup) {
        String em=email.getText().toString().trim(), pw=password.getText().toString();
        if(em.isEmpty() || pw.length()<6){ message.setText(tr("ኢሜይልና ቢያንስ 6 ፊደል ያለው የይለፍ ቃል ያስገቡ።","Enter email and a password of at least 6 characters.")); return; }
        io.execute(() -> {
            try {
                JSONObject body=new JSONObject(); body.put("email",em); body.put("password",pw);
                String path=signup?"/auth/v1/signup":"/auth/v1/token?grant_type=password";
                JSONObject result=request("POST",path,body,null);
                String token=result.optString("access_token","");
                JSONObject u=result.optJSONObject("user");
                if(token.isEmpty() && u!=null) {
                    main.post(() -> message.setText(tr("ምዝገባው ተልኳል። ኢሜይልዎን ያረጋግጡ።","Sign-up submitted. Check your email to confirm.")));
                    return;
                }
                if(token.isEmpty()) throw new Exception("No session returned. Confirm your email, then sign in.");
                String id=u==null?"":u.optString("id","");
                accessToken=token; userId=id;
                getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("access_token",token).putString("user_id",id).apply();
                main.post(() -> { message.setText(tr("በተሳካ ሁኔታ ገብተዋል።","Signed in successfully.")); accountLabel.setText(tr("ገብተዋል","Signed in")); });
            } catch(Exception ex){ main.post(() -> message.setText(ex.getMessage())); }
        });
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4102 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            message.setText(tr("ፎቶ ተመርጧል።","Photo selected."));
        }
    }
    private String uploadProductImage(Uri uri) throws Exception {
        String mime = getContentResolver().getType(uri);
        if (mime == null || !mime.startsWith("image/")) throw new Exception("Choose an image file.");
        String ext = mime.equals("image/png") ? ".png" : mime.equals("image/webp") ? ".webp" : ".jpg";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) throw new Exception("Could not read selected image.");
            byte[] buf = new byte[8192]; int n;
            while ((n = in.read(buf)) != -1) {
                bytes.write(buf, 0, n);
                if (bytes.size() > 4 * 1024 * 1024) throw new Exception("Image must be 4 MB or smaller.");
            }
        }
        String objectPath = userId + "/" + System.currentTimeMillis() + ext;
        String encodedPath = URLEncoder.encode(objectPath, "UTF-8").replace("+", "%20").replace("%2F", "/");
        URL url = new URL(API + "/storage/v1/object/product-images/" + encodedPath);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST"); c.setConnectTimeout(20000); c.setReadTimeout(30000); c.setDoOutput(true);
        c.setRequestProperty("apikey", KEY); c.setRequestProperty("Authorization", "Bearer " + accessToken);
        c.setRequestProperty("Content-Type", mime); c.setRequestProperty("x-upsert", "false");
        try (OutputStream out = c.getOutputStream()) { out.write(bytes.toByteArray()); }
        int code = c.getResponseCode();
        InputStream response = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        if (response != null) { byte[] b = new byte[2048]; int n; while ((n = response.read(b)) != -1) result.write(b, 0, n); response.close(); }
        c.disconnect();
        if (code < 200 || code >= 300) throw new Exception(result.toString("UTF-8"));
        return API + "/storage/v1/object/public/product-images/" + encodedPath;
    }
    private void publishProduct() {
        if(accessToken.isEmpty()){ message.setText(tr("ምርት ለመለጠፍ መጀመሪያ ይግቡ።","Please sign in before posting.")); return; }
        try {
            JSONObject p=new JSONObject(); p.put("user_id",userId);
            p.put("name",productName.getText().toString().trim());
            p.put("price",Double.parseDouble(price.getText().toString().trim()));
            p.put("category",category.getText().toString().trim());
            p.put("seller_name",seller.getText().toString().trim());
            p.put("phone",phone.getText().toString().trim());
            p.put("description",description.getText().toString().trim());
            if(p.optString("name").isEmpty()||p.optString("seller_name").isEmpty()||p.optString("phone").isEmpty()) throw new Exception("Complete product, seller and phone fields.");
            final Uri imageToUpload = selectedImageUri;
            io.execute(() -> { try {
                if (imageToUpload != null) p.put("image_url", uploadProductImage(imageToUpload));
                request("POST","/rest/v1/products",p,null);
                selectedImageUri = null;
                main.post(() -> { message.setText(tr("ምርቱ ተለጥፏል።","Product published.")); loadProducts(); });
            } catch(Exception ex){ main.post(() -> message.setText(ex.getMessage())); }});
        } catch(Exception ex){ message.setText(tr("ዋጋውን በትክክል ያስገቡ።","Enter a valid price.")); }
    }
    private JSONObject request(String method,String path,JSONObject body,String query) throws Exception {
        URL url=new URL(API+path+(query==null?"":query));
        HttpURLConnection c=(HttpURLConnection)url.openConnection();
        c.setRequestMethod(method); c.setConnectTimeout(15000); c.setReadTimeout(20000);
        c.setRequestProperty("apikey",KEY);
        if(!accessToken.isEmpty()) c.setRequestProperty("Authorization","Bearer "+accessToken);
        c.setRequestProperty("Content-Type","application/json");
        c.setRequestProperty("Accept","application/json");
        if(body!=null){ c.setDoOutput(true); c.setRequestProperty("Prefer","return=minimal"); try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
        int code=c.getResponseCode(); InputStream stream=code>=200&&code<300?c.getInputStream():c.getErrorStream();
        ByteArrayOutputStream out=new ByteArrayOutputStream(); if(stream!=null){byte[] b=new byte[4096];int n;while((n=stream.read(b))!=-1)out.write(b,0,n);stream.close();}
        String raw=out.toString("UTF-8"); c.disconnect();
        if(code<200||code>=300) throw new Exception(raw.isEmpty()?"Request failed ("+code+")":raw);
        return raw.isEmpty()?new JSONObject():new JSONObject(raw);
    }
    private void loadProducts() {
        if(productList==null)return;
        io.execute(() -> {
            try {
                URL url=new URL(API+"/rest/v1/products?select=*&order=created_at.desc");
                HttpURLConnection c=(HttpURLConnection)url.openConnection();
                c.setConnectTimeout(15000);c.setReadTimeout(20000);
                c.setRequestProperty("apikey",KEY);if(!accessToken.isEmpty())c.setRequestProperty("Authorization","Bearer "+accessToken);
                int code=c.getResponseCode(); if(code<200||code>=300)throw new Exception("Products unavailable ("+code+")");
                ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
                JSONArray arr=new JSONArray(out.toString("UTF-8")); products.clear();for(int i=0;i<arr.length();i++)products.add(arr.getJSONObject(i));c.disconnect();
                main.post(this::renderProducts);
            } catch(Exception ex){ main.post(() -> { if(message!=null)message.setText(tr("ምርቶችን ማምጣት አልተቻለም። ኢንተርኔትን ያረጋግጡ።","Could not load products. Check your internet connection.")); }); }
        });
    }
    private void renderProducts() {
        if(productList==null)return; productList.removeAllViews();
        String q=search==null?"":search.getText().toString().toLowerCase(Locale.ROOT);
        for(JSONObject p:products){
            String name=p.optString("name"), cat=p.optString("category"), desc=p.optString("description");
            if(!(name+" "+cat+" "+desc).toLowerCase(Locale.ROOT).contains(q))continue;
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(14),dp(12),dp(14),dp(12));
            card.setBackgroundColor(Color.WHITE);
            String imageUrl = p.optString("image_url", "");
            if (!imageUrl.isEmpty()) {
                ImageView photo = new ImageView(this);
                photo.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(180)));
                photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                card.addView(photo);
                io.execute(() -> { try {
                    Bitmap bitmap = BitmapFactory.decodeStream(new URL(imageUrl).openStream());
                    if (bitmap != null) main.post(() -> photo.setImageBitmap(bitmap));
                } catch (Exception ignored) {} });
            }
            TextView title=text(name,18,true,Color.rgb(20,40,70));card.addView(title);
            card.addView(text(cat,12,false,Color.GRAY));
            card.addView(text(String.format(Locale.US,"%,.0f ETB",p.optDouble("price")),19,true,Color.rgb(37,99,235)));
            card.addView(text(desc,14,false,Color.rgb(76,91,113)));
            card.addView(text(p.optString("seller_name"),13,false,Color.GRAY));
            Button contact=button(tr("ሻጩን በWhatsApp ያግኙ","Contact seller on WhatsApp"),true);
            String digits=p.optString("phone").replaceAll("[^0-9]","");
            contact.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/"+digits)));}catch(Exception ignored){}});
            card.addView(contact);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);
            productList.addView(card,lp);
        }
        if(productList.getChildCount()==0)productList.addView(text(tr("ምርት አልተገኘም።","No products found."),14,false,Color.GRAY));
    }
    @Override protected void onDestroy(){super.onDestroy();io.shutdownNow();}
}
