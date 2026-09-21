package com.adreesulhassan.puretasbeeh.data.db;

import android.content.Context;

import com.adreesulhassan.puretasbeeh.data.entity.BookEntity;
import com.adreesulhassan.puretasbeeh.data.entity.ContentCategory;
import com.adreesulhassan.puretasbeeh.data.entity.ContentEntity;
import com.adreesulhassan.puretasbeeh.data.entity.HadithEntity;
import com.adreesulhassan.puretasbeeh.data.entity.SectTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Curated offline seed — Divine Pearls–style duas/ziyaraat/munajat,
 * Sahifa, Hisnul/Mafatih excerpts, and 1000+ ahadith from asset DB.
 */
public final class DatabaseSeeder {

    private DatabaseSeeder() {
    }

    public static void seed(Context context, AppDatabase db) {
        android.content.SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences("pure_tasbeeh_prefs", Context.MODE_PRIVATE);
        final int packVersion = 4; // bump when ahadith/duas asset packs change
        int installed = prefs.getInt("content_pack_version", 0);

        List<ContentEntity> packed = ContentAssetLoader.loadAll(context);
        if (!packed.isEmpty() && (db.contentDao().count() < 200 || installed < packVersion)) {
            db.contentDao().deleteAll();
            db.contentDao().insertAll(packed);
        } else if (db.contentDao().count() == 0) {
            db.contentDao().insertAll(buildContent());
        }
        if (db.bookDao().count() == 0) {
            db.bookDao().insertAll(buildBooks());
        }

        List<HadithEntity> fromAsset = HadithAssetLoader.loadAll(context);
        if (!fromAsset.isEmpty() && (db.hadithDao().count() < 900 || installed < packVersion)) {
            db.hadithDao().deleteAll();
            db.hadithDao().insertAll(fromAsset);
        } else if (db.hadithDao().count() == 0) {
            db.hadithDao().insertAll(buildAhadithFallback());
        }

        if (installed < packVersion) {
            prefs.edit().putInt("content_pack_version", packVersion).apply();
        }
    }

    private static ContentEntity dua(String title, String ar, String en, String ur, String fa,
                                     String source, String sect, String cat) {
        return new ContentEntity(title, ar, en, ur, ar, fa, source, sect, cat);
    }

    private static HadithEntity hadith(String ar, String en, String ur, String fa,
                                       String source, String sect) {
        return new HadithEntity(ar, en, ur, ar, fa, source, sect);
    }

    private static List<ContentEntity> buildContent() {
        List<ContentEntity> list = new ArrayList<>();
        list.addAll(buildNamaz());
        list.addAll(buildSpecial());
        list.addAll(buildZiyaraat());
        list.addAll(buildMunajat());
        list.addAll(buildDaily());
        list.addAll(buildAamal());
        list.addAll(buildSahifa());
        return list;
    }

    private static List<ContentEntity> buildNamaz() {
        List<ContentEntity> list = new ArrayList<>();
        // —— SHIA / Mafatih after Salah ——
        list.add(dua("Tasbih of Fatima (after Salah)",
                "اللَّهُ أَكْبَرُ (٣٤) · الْحَمْدُ لِلَّهِ (٣٣) · سُبْحَانَ اللَّهِ (٣٣)",
                "After prayer: Allahu Akbar 34, Alhamdulillah 33, Subhanallah 33 — Tasbih az-Zahra.",
                "نماز کے بعد: اللہ اکبر ۳۴، الحمد للہ ۳۳، سبحان اللہ ۳۳ — تسبیح زہرا۔",
                "پس از نماز: الله اکبر ۳۴، الحمدلله ۳۳، سبحان‌الله ۳۳ — تسبیح زهرا.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.NAMAZ));
        list.add(dua("Salawat after Salah (Mafatih)",
                "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ",
                "O Allah, send blessings upon Muhammad and the family of Muhammad.",
                "اے اللہ، محمد و آل محمد پر درود بھیج۔",
                "خدایا بر محمد و آل محمد درود فرست.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.NAMAZ));
        list.add(dua("Ayatul Kursi (after Salah)",
                "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ",
                "Allah — there is no deity except Him, the Ever-Living, the Sustainer. Neither drowsiness overtakes Him nor sleep.",
                "اللہ — اس کے سوا کوئی معبود نہیں، زندہ، سب کا تھامنے والا۔ اسے نہ اونگھ آتی ہے نہ نیند۔",
                "خداست که معبودی جز او نیست، زنده پاینده. نه چرت او را می‌گیرد نه خواب.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.NAMAZ));
        list.add(dua("Dua after Obligatory Prayer",
                "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
                "There is no god but Allah alone, no partner. His is the dominion and praise. He gives life and causes death, and He is over all things competent.",
                "اللہ کے سوا کوئی معبود نہیں اکیلا، اس کا کوئی شریک نہیں۔ بادشاہی اور تعریف اسی کی ہے…",
                "معبودی جز الله یکتا نیست، شریکی ندارد. فرمانروایی و ستایش از آن اوست…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.NAMAZ));
        list.add(dua("Seeking Acceptance of Prayer",
                "رَبَّنَا تَقَبَّلْ مِنَّا ۖ إِنَّكَ أَنْتَ السَّمِيعُ الْعَلِيمُ",
                "Our Lord, accept from us. Indeed You are the Hearing, the Knowing.",
                "اے ہمارے رب ہم سے قبول فرما، بیشک تو سننے والا جاننے والا ہے۔",
                "پروردگارا از ما بپذیر که تو شنوای دانایی.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.NAMAZ));
        list.add(dua("Ta'qeebat — Praise after Prayer",
                "سُبْحَانَ رَبِّيَ الْعَظِيمِ وَبِحَمْدِهِ · سُبْحَانَ رَبِّيَ الْأَعْلَىٰ وَبِحَمْدِهِ",
                "Glory be to my Lord, the Magnificent, and with His praise. Glory be to my Lord, the Most High, and with His praise.",
                "پاک ہے میرا رب عظیم اور اس کی تعریف کے ساتھ۔ پاک ہے میرا رب اعلیٰ…",
                "منزه است پروردگار عظیم من و به ستایش او…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.NAMAZ));

        // —— SUNNI / Hisnul Muslim after Salah ——
        list.add(dua("Istighfar after Salah (×3)",
                "أَسْتَغْفِرُ اللَّهَ · أَسْتَغْفِرُ اللَّهَ · أَسْتَغْفِرُ اللَّهَ",
                "I seek Allah’s forgiveness (three times) after each obligatory prayer.",
                "ہر فرض نماز کے بعد تین بار استغفار۔",
                "پس از هر نماز واجب سه بار استغفار.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.NAMAZ));
        list.add(dua("Allahumma Antas-Salam",
                "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
                "O Allah, You are Peace and from You is peace. Blessed are You, O Owner of majesty and honour.",
                "اے اللہ تو سلامتی ہے اور تیری طرف سے سلامتی ہے۔ مبارک ہے تو اے جلال و اکرام والے!",
                "خدایا تویی سلام و از توست سلام. خجسته‌ای ای صاحب جلال و اکرام.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.NAMAZ));
        list.add(dua("Ayatul Kursi after Salah",
                "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
                "Whoever recites Ayatul Kursi after every obligatory prayer, nothing prevents him from entering Paradise except death.",
                "ہر فرض نماز کے بعد آیت الکرسی — جنت میں داخلے میں موت کے سوا کوئی رکاوٹ نہیں۔",
                "پس از هر نماز واجب آیت‌الکرسی — میان او و بهشت جز مرگ مانعی نیست.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.NAMAZ));
        list.add(dua("Tasbih after Salah (33×3)",
                "سُبْحَانَ اللَّهِ (٣٣) · الْحَمْدُ لِلَّهِ (٣٣) · اللَّهُ أَكْبَرُ (٣٣) · لَا إِلَٰهَ إِلَّا اللَّهُ…",
                "Subhanallah 33, Alhamdulillah 33, Allahu Akbar 33, then complete with Tawhid.",
                "سبحان اللہ ۳۳، الحمد للہ ۳۳، اللہ اکبر ۳۳، پھر توحید۔",
                "سبحان‌الله ۳۳، الحمدلله ۳۳، الله اکبر ۳۳، سپس توحید.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.NAMAZ));
        list.add(dua("Mu'awwidhat after Salah",
                "قُلْ هُوَ اللَّهُ أَحَدٌ · قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ · قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
                "Recite Surah al-Ikhlas, al-Falaq, and an-Nas after each prayer.",
                "ہر نماز کے بعد اخلاص، فلق اور ناس پڑھیں۔",
                "پس از هر نماز اخلاص، فلق و ناس بخوانید.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.NAMAZ));
        list.add(dua("Dua for Guidance in Prayer",
                "اللَّهُمَّ أَعِنِّي عَلَىٰ ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
                "O Allah, help me to remember You, thank You, and worship You well.",
                "اے اللہ مجھے اپنے ذکر، شکر اور اچھی عبادت پر مدد دے۔",
                "خدایا مرا بر یاد و شکر و نیک عبادتت یاری کن.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.NAMAZ));
        return list;
    }

    private static List<ContentEntity> buildSpecial() {
        List<ContentEntity> list = new ArrayList<>();
        list.add(dua("Dua Istikhara",
                "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ",
                "O Allah, I seek the best from You by Your knowledge, and I seek ability by Your power, and I ask You of Your great bounty.",
                "اے اللہ میں تیرے علم سے خیر مانگتا ہوں اور تیری قدرت سے طاقت چاہتا ہوں…",
                "خدایا از علم تو خیر می‌جویم و از قدرت تو توانا می‌خواهم…",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.SPECIAL));
        list.add(dua("Dua for Travel",
                "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنْقَلِبُونَ",
                "Glory to Him Who has subjected this to us, and we could not have done it by ourselves. Indeed, to our Lord we will return.",
                "پاک ہے وہ جس نے اسے ہمارے لیے مسخر کیا… اور بیشک ہم اپنے رب کی طرف لوٹنے والے ہیں۔",
                "منزه است آنکه این را برای ما رام کرد… و ما به سوی پروردگارمان بازمی‌گردیم.",
                "Hisnul Muslim / Mafatih", SectTag.BOTH, ContentCategory.SPECIAL));
        list.add(dua("Dua for Sorrow and Anxiety",
                "اللَّهُمَّ إِنِّي عَبْدُكَ، ابْنُ عَبْدِكَ، ابْنُ أَمَتِكَ… أَسْأَلُكَ بِكُلِّ اسْمٍ هُوَ لَكَ… أَنْ تَجْعَلَ الْقُرْآنَ رَبِيعَ قَلْبِي",
                "O Allah, I am Your servant… I ask You by every Name that is Yours… to make the Quran the spring of my heart.",
                "اے اللہ میں تیرا بندہ ہوں… تجھ سے تیرے ہر نام کے وسیلے سے مانگتا ہوں کہ قرآن کو میرے دل کی بہار بنا دے۔",
                "خدایا من بنده‌ام… به هر نامی که از آن توست می‌خواهم که قرآن را بهار دلم گردانی.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.SPECIAL));
        list.add(dua("Dua Kumayl (opening)",
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ وَبِقُوَّتِكَ الَّتِي قَهَرْتَ بِهَا كُلَّ شَيْءٍ",
                "O Allah, I ask You by Your mercy which encompasses all things, and by Your power by which You dominate all things.",
                "اے اللہ میں تیری اس رحمت کا واسطہ دے کر مانگتا ہوں جو ہر چیز پر وسیع ہے…",
                "خدایا به رحمتی که همه چیز را فراگرفته از تو می‌خواهم…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Munajat of the Repenters (opening)",
                "إِلَٰهِي أَلْبَسَتْنِي الْخَطَايَا ثَوْبَ مَذَلَّتِي، وَجَلَّلَنِي الْبُعْدُ مِنْكَ لِبَاسَ مَسْكَنَتِي",
                "My God, sins have clothed me in the garment of my lowliness, and distance from You has wrapped me in the dress of my poverty.",
                "اے میرے معبود، گناہوں نے مجھے ذلت کا لباس پہنایا…",
                "خدایا گناهان جامه خواری بر من پوشاند…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Dua for Healing",
                "اللَّهُمَّ رَبَّ النَّاسِ، أَذْهِبِ الْبَأْسَ، اشْفِهِ وَأَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ",
                "O Allah, Lord of mankind, remove the harm and heal him — You are the Healer. There is no healing but Your healing.",
                "اے اللہ لوگوں کے رب، تکلیف دور کر اور شفا دے — تو ہی شافی ہے۔",
                "خدایا پروردگار مردم، سختی را ببر و شفا ده — تو شفادهنده‌ای.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.SPECIAL));
        list.add(dua("Dua when Entering the Market",
                "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ وَهُوَ حَيٌّ لَا يَمُوتُ",
                "There is no god but Allah alone… He gives life and causes death and He is Living and does not die.",
                "بازار میں داخل ہوتے وقت توحید کی یہ دعا۔",
                "هنگام ورود به بازار این ذکر توحید.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.SPECIAL));
        list.add(dua("Dua for Rain",
                "اللَّهُمَّ أَسْقِنَا غَيْثًا مُغِيثًا مَرِيئًا مَرِيعًا نَافِعًا غَيْرَ ضَارٍّ",
                "O Allah, give us rain that is helpful, wholesome, abundant, beneficial and not harmful.",
                "اے اللہ ہمیں ایسی بارش دے جو مددگار، پاکیزہ، نفع بخش ہو نقصان دہ نہ ہو۔",
                "خدایا بارانی سودمند و گوارا و فراوان به ما بده که زیان‌بار نباشد.",
                "Hisnul Muslim / Mafatih", SectTag.BOTH, ContentCategory.SPECIAL));
        list.add(dua("Ziyarat / Salawat for Difficulties",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِ مُحَمَّدٍ وَعَجِّلْ فَرَجَهُمْ",
                "O Allah, bless Muhammad and the family of Muhammad and hasten their relief.",
                "اے اللہ محمد و آل محمد پر درود بھیج اور ان کی فرج جلد فرما۔",
                "خدایا بر محمد و آل محمد درود فرست و فرجشان را شتاب ده.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Dua for Protection",
                "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                "In the Name of Allah with Whose Name nothing on earth or in heaven can cause harm, and He is the Hearing, the Knowing.",
                "اللہ کے نام سے جس کے نام کے ساتھ زمین و آسمان میں کوئی چیز نقصان نہیں پہنچا سکتی۔",
                "به نام خدایی که با نام او چیزی در زمین و آسمان زیان نرساند.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.SPECIAL));
        list.add(dua("Dua Kumail",
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ وَبِقُوَّتِكَ الَّتِي قَهَرْتَ بِهَا كُلَّ شَيْءٍ",
                "O Allah, I ask You by Your mercy which encompasses all things… (Dua Kumail opening)",
                "اے اللہ میں تیری وسیع رحمت کا واسطہ دے کر مانگتا ہوں…",
                "خدایا به رحمت فراگیرت از تو می‌خواهم…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Dua Tawassul",
                "اَللَّهُمَّ إِنِّي أَسْأَلُكَ وَأَتَوَجَّهُ إِلَيْكَ بِنَبِيِّكَ نَبِيِّ الرَّحْمَةِ مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَآلِهِ",
                "O Allah, I ask You and turn to You through Your Prophet, the Prophet of mercy, Muhammad…",
                "اے اللہ میں تیرے نبی رحمت محمد کے وسیلے سے تجھے پکارتا ہوں…",
                "خدایا به پیامبر رحمت محمد به تو روی می‌آورم…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Dua Nudbah",
                "أَيْنَ الْحَسَنُ أَيْنَ الْحُسَيْنُ… أَيْنَ بَقِيَّةُ اللَّهِ الَّتِي لَا تَخْلُو مِنَ الْعِتْرَةِ الْهَادِيَةِ",
                "Where is Hasan, where is Husayn… Where is the remnant of Allah who is never absent from the guiding family…",
                "حسن کہاں ہیں، حسین کہاں ہیں… بقیۃ اللہ کہاں ہیں…",
                "حسن کجاست، حسین کجاست… بقیةالله کجاست…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Dua Faraj",
                "اَللَّهُمَّ كُنْ لِوَلِيِّكَ الْحُجَّةِ ابْنِ الْحَسَنِ… فِي هَٰذِهِ السَّاعَةِ وَفِي كُلِّ سَاعَةٍ وَلِيًّا وَحَافِظًا",
                "O Allah, be for Your wali, the Hujjah ibn al-Hasan… in this hour and every hour a guardian and protector.",
                "اے اللہ اپنے ولی حجت ابن الحسن کے لیے سرپرست و نگہبان بن…",
                "خدایا برای ولی‌ات حجت بن الحسن… ولی و نگهبان باش.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Hadith e Kisa (opening)",
                "عَنْ فَاطِمَةَ الزَّهْرَاءِ… أَنَّهَا قَالَتْ: دَخَلَ عَلَيَّ أَبِي رَسُولُ اللَّهِ…",
                "From Fatima az-Zahra… that she said: My father the Messenger of Allah entered upon me…",
                "فاطمہ زہرا سے روایت… میرے والد رسول اللہ میرے پاس تشریف لائے…",
                "از فاطمه زهرا… که پدرم رسول خدا بر من وارد شد…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Dua Sabah",
                "اَللَّهُمَّ يَا مَنْ دَلَعَ لِسَانَ الصَّبَاحِ بِنُطْقِ تَبَهُّجِهِ…",
                "O Allah, O He Who rolled out the tongue of morning with the speech of its delight…",
                "اے اللہ اے وہ جس نے صبح کی زبان کو خوشی کی گفتگو سے کھولا…",
                "خدایا ای آنکه زبان صبح را به شادمانی گشود…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Dua Jawshan Kabir (opening)",
                "اَللَّهُمَّ إِنِّي أَسْأَلُكَ بِاسْمِكَ يَا اللَّهُ يَا رَحْمَٰنُ يَا رَحِيمُ يَا كَرِيمُ…",
                "O Allah, I ask You by Your Name: O Allah, O Most Merciful, O Most Compassionate, O Generous…",
                "اے اللہ میں تیرے نام سے مانگتا ہوں: یا اللہ یا رحمان یا رحیم…",
                "خدایا به نامت می‌خواهم: یا الله یا رحمان یا رحیم…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Dua Iftitah",
                "اَللَّهُمَّ إِنِّي أَفْتَتِحُ الثَّنَاءَ بِحَمْدِكَ…",
                "O Allah, I open praise with Your hamd… (Ramadan opening dua)",
                "اے اللہ میں تیری تعریف سے ثنا شروع کرتا ہوں…",
                "خدایا ثنا را با حمدت آغاز می‌کنم…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        list.add(dua("Dua Abu Hamza Thumali (opening)",
                "إِلَٰهِي لَا تُؤَدِّبْنِي بِعُقُوبَتِكَ، وَلَا تَمْكُرْ بِي فِي حِيلَتِكَ",
                "My God, do not discipline me with Your punishment, and do not scheme against me in Your plan…",
                "اے میرے معبود مجھے اپنے عذاب سے نہ سزا دے…",
                "خدایا مرا با عقوبتت ادب مکن…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.SPECIAL));
        return list;
    }

    private static List<ContentEntity> buildDaily() {
        List<ContentEntity> list = new ArrayList<>();
        list.add(dua("Morning Adhkar — Opening",
                "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
                "We have entered the morning and the dominion belongs to Allah. All praise is for Allah. There is no god but Allah alone.",
                "ہم نے صبح کی اور بادشاہی اللہ کی ہے۔ سب تعریف اللہ کے لیے ہے۔",
                "صبح کردیم و فرمانروایی از آن خداست. ستایش خدا را.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.DAILY));
        list.add(dua("Evening Adhkar — Opening",
                "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
                "We have entered the evening and the dominion belongs to Allah. All praise is for Allah.",
                "ہم نے شام کی اور بادشاہی اللہ کی ہے۔",
                "شام کردیم و فرمانروایی از آن خداست.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.DAILY));
        list.add(dua("Sayyid al-Istighfar",
                "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
                "O Allah, You are my Lord, there is no god but You. You created me and I am Your servant, and I am upon Your covenant as much as I can.",
                "اے اللہ تو میرا رب ہے… تو نے مجھے پیدا کیا اور میں تیرا بندہ ہوں۔",
                "خدایا تویی پروردگارم… مرا آفریدی و من بنده‌ام.",
                "Hisnul Muslim", SectTag.BOTH, ContentCategory.DAILY));
        list.add(dua("Morning Dua (Mafatih)",
                "أَصْبَحْتُ اللَّهُمَّ مُعْتَصِمًا بِعَهْدِكَ وَذِمَّتِكَ مِنْ شَرِّ كُلِّ ذِي شَرٍّ",
                "O Allah, I have entered the morning seeking protection by Your covenant from the evil of every evil one.",
                "اے اللہ میں نے صبح تیری پناہ میں کی ہر شر والے کے شر سے۔",
                "خدایا صبح کردم در پناه عهد تو از شر هر بدی.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.DAILY));
        list.add(dua("Evening Dua (Mafatih)",
                "أَمْسَيْتُ اللَّهُمَّ مُعْتَصِمًا بِعَهْدِكَ وَذِمَّتِكَ مِنْ شَرِّ كُلِّ ذِي شَرٍّ",
                "O Allah, I have entered the evening seeking protection by Your covenant from the evil of every evil one.",
                "اے اللہ میں نے شام تیری پناہ میں کی۔",
                "خدایا شام کردم در پناه عهد تو.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.DAILY));
        list.add(dua("Before Sleep",
                "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                "In Your Name, O Allah, I die and I live.",
                "اے اللہ تیرے نام سے میں مرتا اور جیتا ہوں۔",
                "خدایا به نام تو می‌میرم و زنده می‌شوم.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.DAILY));
        list.add(dua("Upon Waking",
                "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                "All praise is for Allah Who gave us life after He caused us to die, and to Him is the resurrection.",
                "سب تعریف اللہ کے لیے جس نے ہمیں موت کے بعد زندگی دی۔",
                "ستایش خدایی را که پس از مرگمان زنده کرد.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.DAILY));
        list.add(dua("Entering / Leaving Home",
                "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                "In the Name of Allah, I place my trust in Allah. There is no power nor strength except with Allah.",
                "اللہ کے نام سے، میں نے اللہ پر بھروسہ کیا۔",
                "به نام خدا، بر خدا توکل کردم.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.DAILY));
        list.add(dua("Before Eating",
                "بِسْمِ اللَّهِ",
                "In the Name of Allah. (If forgotten at start: Bismillahi awwalahu wa akhirahu.)",
                "کھانے سے پہلے بسم اللہ۔",
                "پیش از غذا بسم الله.",
                "Hisnul Muslim", SectTag.BOTH, ContentCategory.DAILY));
        list.add(dua("After Eating",
                "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَٰذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
                "All praise is for Allah Who fed me this and provided it for me without any power or strength from me.",
                "سب تعریف اللہ کے لیے جس نے مجھے یہ کھلایا۔",
                "ستایش خدایی را که این را به من خوراند.",
                "Hisnul Muslim", SectTag.SUNNI, ContentCategory.DAILY));
        return list;
    }

    private static List<ContentEntity> buildAamal() {
        List<ContentEntity> list = new ArrayList<>();
        list.add(dua("Aamal of Ramadan (opening)",
                "اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالْأَمْنِ وَالْإِيمَانِ وَالسَّلَامَةِ وَالْإِسْلَامِ",
                "O Allah, let it rise over us with security, faith, safety and Islam. (Ramadan chapter — Mafatih)",
                "اے اللہ اسے ہم پر امن، ایمان، سلامتی اور اسلام کے ساتھ طلوع فرما۔",
                "خدایا آن را با امنیت و ایمان و سلامت و اسلام بر ما طلوع ده.",
                "Mafatih al-Jinan", SectTag.BOTH, ContentCategory.AAMAL));
        list.add(dua("Aamal of Rajab",
                "يَا مَنْ أَرْجُوهُ لِكُلِّ خَيْرٍ، وَآمَنُ سَخَطَهُ عِنْدَ كُلِّ شَرٍّ",
                "O He Whom I hope for every good… (Rajab chapter — Mafatih)",
                "اے وہ جس سے میں ہر خیر کی امید رکھتا ہوں…",
                "ای آنکه به او برای هر خیری امید دارم…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.AAMAL));
        list.add(dua("Aamal of Sha'ban / 15 Sha'ban",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِ مُحَمَّدٍ، وَاشْغَلْنَا بِذِكْرِكَ عَنْ كُلِّ ذِكْرٍ",
                "O Allah, bless Muhammad and his family, and occupy us with Your remembrance.",
                "اے اللہ محمد و آل محمد پر درود بھیج اور ہمیں اپنے ذکر میں مصروف رکھ۔",
                "خدایا بر محمد و آل محمد درود فرست و ما را به ذکرت مشغول دار.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.AAMAL));
        list.add(dua("Aamal of Ashura",
                "اللَّهُمَّ اجْعَلْنَا مِمَّنْ يَنْتَصِرُ لِدِينِكَ وَيَنْصُرُ أَوْلِيَاءَكَ",
                "O Allah, make us among those who support Your religion and aid Your friends.",
                "اے اللہ ہمیں اپنے دین کی نصرت کرنے والوں میں سے بنا۔",
                "خدایا ما را از یاری‌کنندگان دینت قرار ده.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.AAMAL));
        list.add(dua("Aamal of Friday (Jumah)",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِ مُحَمَّدٍ وَعَجِّلْ فَرَجَهُمْ",
                "Friday aamal opening — blessings and hastening of relief (Mafatih).",
                "جمعہ کے اعمال — درود اور فرج کی دعا۔",
                "اعمال جمعه — درود و دعای فرج.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.AAMAL));
        list.add(dua("Aamal of Ghadeer",
                "الْحَمْدُ لِلَّهِ الَّذِي جَعَلَنَا مِنَ الْمُتَمَسِّكِينَ بِوِلَايَةِ أَمِيرِ الْمُؤْمِنِينَ وَالْأَئِمَّةِ عَلَيْهِمُ السَّلَامُ",
                "Praise be to Allah Who made us among those holding to the wilayah of Amir al-Mu'minin and the Imams.",
                "سب تعریف اللہ کے لیے جس نے ہمیں امیر المؤمنین کی ولایت پر قائم رکھا۔",
                "ستایش خدایی را که ما را از متمسکین به ولایت امیرالمؤمنین قرار داد.",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.AAMAL));
        list.add(dua("Dua for Muharram",
                "يَا اللَّهُ يَا رَحْمَٰنُ يَا رَحِيمُ يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَىٰ دِينِكَ",
                "O Allah, O Most Merciful… keep my heart firm upon Your religion.",
                "اے اللہ… میرے دل کو اپنے دین پر ثابت رکھ۔",
                "خدایا… دلم را بر دینت ثابت بدار.",
                "Mafatih / Hisnul", SectTag.BOTH, ContentCategory.AAMAL));
        return list;
    }

    private static List<ContentEntity> buildZiyaraat() {
        List<ContentEntity> list = new ArrayList<>();
        list.add(dua("Ziyarat Ashura",
                "اَلسَّلَامُ عَلَيْكَ يَا أَبَا عَبْدِ اللَّهِ، اَلسَّلَامُ عَلَيْكَ يَا ابْنَ رَسُولِ اللَّهِ…",
                "Peace be upon you, O Aba Abdillah; peace be upon you, O son of the Messenger of Allah…",
                "اے ابا عبداللہ آپ پر سلام… رسول اللہ کے بیٹے آپ پر سلام…",
                "سلام بر تو ای اباعبدالله… سلام بر تو ای فرزند رسول خدا…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.ZIYARAAT));
        list.add(dua("Ziyarat Warith",
                "اَلسَّلَامُ عَلَيْكَ يَا وَارِثَ آدَمَ صَفْوَةِ اللَّهِ، اَلسَّلَامُ عَلَيْكَ يَا وَارِثَ نُوحٍ نَبِيِّ اللَّهِ",
                "Peace be upon you, O heir of Adam, the chosen of Allah; peace be upon you, O heir of Noah…",
                "اے آدم صفوۃ اللہ کے وارث آپ پر سلام…",
                "سلام بر تو ای وارث آدم برگزیده خدا…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.ZIYARAAT));
        list.add(dua("Ziyarat Ameenullah",
                "اَلسَّلَامُ عَلَيْكَ يَا أَمِينَ اللَّهِ فِي أَرْضِهِ وَحُجَّتَهُ عَلَىٰ عِبَادِهِ",
                "Peace be upon you, O trustee of Allah on His earth and His proof over His servants.",
                "اے زمین میں اللہ کے امین آپ پر سلام…",
                "سلام بر تو ای امین خدا در زمینش…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.ZIYARAAT));
        list.add(dua("Ziyarat Arbaeen",
                "اَلسَّلَامُ عَلَىٰ وَلِيِّ اللَّهِ وَحَبِيبِهِ، اَلسَّلَامُ عَلَىٰ خَلِيلِ اللَّهِ وَنَجِيبِهِ",
                "Peace be upon the friend of Allah and His beloved; peace be upon the intimate of Allah…",
                "اللہ کے ولی اور حبیب پر سلام…",
                "سلام بر ولی خدا و حبیب او…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.ZIYARAAT));
        list.add(dua("Ziyarat Aale Yasin",
                "سَلَامٌ عَلَىٰ آلِ يس، اَلسَّلَامُ عَلَيْكَ يَا دَاعِيَ اللَّهِ وَرَبَّانِيَّ آيَاتِهِ",
                "Peace upon the family of Yasin. Peace be upon you, O caller to Allah…",
                "آل یٰس پر سلام… اے اللہ کی طرف بلانے والے آپ پر سلام…",
                "سلام بر آل یاسین… ای دعوت‌کننده به خدا…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.ZIYARAAT));
        list.add(dua("Ziyarat Jami'ah Kabirah (opening)",
                "اَلسَّلَامُ عَلَيْكُمْ يَا أَهْلَ بَيْتِ النُّبُوَّةِ، وَمَوْضِعَ الرِّسَالَةِ، وَمُخْتَلَفَ الْمَلَائِكَةِ",
                "Peace be upon you, O people of the house of Prophethood, the locus of the Message…",
                "اے اہل بیت نبوت آپ پر سلام…",
                "سلام بر شما ای اهل بیت نبوت…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.ZIYARAAT));
        list.add(dua("Ziyarat Imam Reza (a)",
                "اَلسَّلَامُ عَلَيْكَ يَا عَلِيَّ بْنَ مُوسَى الرِّضَا… اَلسَّلَامُ عَلَيْكَ يَا إِمَامَ الْهُدَىٰ",
                "Peace be upon you, O Ali ibn Musa al-Rida… Peace be upon you, O Imam of guidance.",
                "اے علی بن موسی الرضا آپ پر سلام…",
                "سلام بر تو ای علی بن موسی الرضا…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.ZIYARAAT));
        list.add(dua("Ziyarat Imam Zamana (a)",
                "اَلسَّلَامُ عَلَيْكَ يَا بَقِيَّةَ اللَّهِ فِي أَرْضِهِ… اَلسَّلَامُ عَلَيْكَ يَا صَاحِبَ الزَّمَانِ",
                "Peace be upon you, O remnant of Allah on His earth… Peace be upon you, O Master of the Age.",
                "اے بقیۃ اللہ آپ پر سلام… اے صاحب الزمان آپ پر سلام…",
                "سلام بر تو ای بقیةالله… ای صاحب‌الزمان…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.ZIYARAAT));
        return list;
    }

    private static List<ContentEntity> buildMunajat() {
        List<ContentEntity> list = new ArrayList<>();
        list.add(dua("Munajat Sha'baniyah",
                "اَللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِ مُحَمَّدٍ، وَاسْمَعْ دُعَائِي إِذَا دَعَوْتُكَ، وَاسْمَعْ نِدَائِي إِذَا نَادَيْتُكَ",
                "O Allah, bless Muhammad and his family, hear my dua when I call You, and hear my cry when I cry to You.",
                "اے اللہ میری دعا سن جب میں تجھے پکاروں…",
                "خدایا دعایم را بشنو هنگامی که تو را می‌خوانم…",
                "Mafatih al-Jinan", SectTag.SHIA, ContentCategory.MUNAJAAT));
        list.add(dua("Munajat of the Repenters",
                "إِلَٰهِي أَلْبَسَتْنِي الْخَطَايَا ثَوْبَ مَذَلَّتِي، وَجَلَّلَنِي الْبُعْدُ مِنْكَ لِبَاسَ مَسْكَنَتِي",
                "My God, sins have clothed me in lowliness; distance from You has wrapped me in poverty.",
                "اے میرے معبود، گناہوں نے مجھے ذلت کا لباس پہنایا…",
                "خدایا گناهان جامه خواری بر من پوشاند…",
                "Sahifa Sajjadiya / Mafatih", SectTag.BOTH, ContentCategory.MUNAJAAT));
        list.add(dua("Munajat of the Fearful",
                "إِلَٰهِي أَتُرَاكَ بَعْدَ الْإِيمَانِ بِكَ تُعَذِّبُنِي…",
                "My God, would You punish me after I have believed in You…",
                "اے میرے معبود، کیا ایمان کے بعد تو مجھے عذاب دے گا…",
                "خدایا آیا پس از ایمان به تو عذابم می‌کنی…",
                "Sahifa Sajjadiya", SectTag.BOTH, ContentCategory.MUNAJAAT));
        list.add(dua("Munajat of the Hopeful",
                "إِلَٰهِي مَا أَلَذَّ خَوَاطِرَ الْإِلْهَامِ بِرَجَائِكَ…",
                "My God, how sweet are the thoughts inspired by hope in You…",
                "اے میرے معبود، تیری امید کی باتیں کتنی میٹھی ہیں…",
                "خدایا چه شیرین است اندیشه‌های امید به تو…",
                "Sahifa Sajjadiya", SectTag.BOTH, ContentCategory.MUNAJAAT));
        list.add(dua("Munajat of the Thankful",
                "إِلَٰهِي أَذْهَلَنِي عَنْ إِقَامَةِ شُكْرِكَ تَتَابُعُ طَوْلِكَ",
                "My God, the succession of Your kindness has made me neglect establishing thanks to You…",
                "اے میرے معبود، تیری مہربانیوں کے سلسلے نے مجھے شکر سے غافل کر دیا…",
                "خدایا پیاپی بودن بخششت مرا از شکر بازداشت…",
                "Sahifa Sajjadiya", SectTag.BOTH, ContentCategory.MUNAJAAT));
        list.add(dua("Munajat of the Devotees",
                "إِلَٰهِي مَنْ ذَا الَّذِي ذَاقَ حَلَاوَةَ مَحَبَّتِكَ فَرَامَ مِنْكَ بَدَلًا",
                "My God, who has tasted the sweetness of Your love and then sought a substitute for You?",
                "اے میرے معبود، کس نے تیری محبت کا مزہ چکھا اور پھر بدل طلب کیا؟",
                "خدایا کیست که شیرینی محبتت را چشید و جایگزینی خواست؟",
                "Sahifa Sajjadiya", SectTag.BOTH, ContentCategory.MUNAJAAT));
        return list;
    }

    private static List<ContentEntity> buildSahifa() {
        List<ContentEntity> list = new ArrayList<>();
        String src = "Sahifa Sajjadiya";
        list.add(dua("1. In Praise of Allah",
                "الْحَمْدُ لِلَّهِ الْأَوَّلِ بِلَا أَوَّلٍ كَانَ قَبْلَهُ، وَالْآخِرِ بِلَا آخِرٍ يَكُونُ بَعْدَهُ، الَّذِي قَصُرَتْ عَنْ رُؤْيَتِهِ أَبْصَارُ النَّاظِرِينَ، وَعَجَزَتْ عَنْ نَعْتِهِ أَوْهَامُ الْوَاصِفِينَ",
                "All praise is for Allah — the First without a first before Him, the Last without a last after Him — Whose vision eyes cannot reach and Whose description minds cannot grasp.",
                "سب تعریف اللہ کے لیے — اول بغیر کسی پہلے کے، آخر بغیر کسی بعد کے…",
                "ستایش خدایی را که اول است بی‌اول، آخر است بی‌آخر…",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("2. Blessing upon Muhammad (s)",
                "وَالْحَمْدُ لِلَّهِ الَّذِي مَنَّ عَلَيْنَا بِمُحَمَّدٍ نَبِيِّهِ صَلَّى اللَّهُ عَلَيْهِ وَآلِهِ دُونَ الْأُمَمِ الْمَاضِيَةِ وَالْقُرُونِ السَّالِفَةِ",
                "And praise be to Allah Who favoured us with Muhammad His Prophet — blessings upon him and his family — above past nations and bygone ages.",
                "اور تعریف اللہ کے لیے جس نے ہمیں اپنے نبی محمد (ص) سے نوازا…",
                "و ستایش خدایی را که ما را به محمد پیامبرش نواخت…",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("3. Blessing upon the Bearers of the Throne",
                "اللَّهُمَّ وَحَمَلَةَ عَرْشِكَ الَّذِينَ لَا يَفْتُرُونَ مِنْ تَسْبِيحِكَ، وَلَا يَسْأَمُونَ مِنْ تَقْدِيسِكَ",
                "O Allah, and the bearers of Your Throne who never cease glorifying You and never tire of sanctifying You.",
                "اے اللہ اور تیرے عرش کے اٹھانے والے جو تیری تسبیح سے نہیں تھکتے…",
                "خدایا و حاملان عرشت که از تسبیحت باز نمی‌ایستند…",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("4. Followers of the Messengers",
                "اللَّهُمَّ وَأَتْبَاعُ الرُّسُلِ وَمُصَدِّقُوهُمْ مِنْ أَهْلِ الْأَرْضِ… فَصَلِّ عَلَيْهِمْ",
                "O Allah, and the followers of the messengers and those who affirmed them from the people of the earth… so bless them.",
                "اے اللہ اور رسولوں کے پیروکار اور ان کی تصدیق کرنے والے… پس ان پر درود بھیج۔",
                "خدایا و پیروان رسولان و تصدیق‌کنندگانشان… پس بر آنان درود فرست.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("5. For Himself and Those Under His Care",
                "اللَّهُمَّ يَا ذَا الْمُلْكِ الْمُتَأَبِّدِ بِالْخُلُودِ… صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِهِ، وَأَبْلِغْ بِإِيمَانِي أَكْمَلَ الْإِيمَانِ",
                "O Allah, O Owner of everlasting dominion… bless Muhammad and his family, and make my faith the most complete faith.",
                "اے اللہ اے ہمیشہ کی بادشاہی والے… میرے ایمان کو کامل ترین ایمان بنا۔",
                "خدایا ای صاحب ملک جاودان… ایمانم را کامل‌ترین ایمان گردان.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("6. Morning and Evening",
                "الْحَمْدُ لِلَّهِ الَّذِي خَلَقَ اللَّيْلَ وَالنَّهَارَ بِقُوَّتِهِ، وَمَيَّزَ بَيْنَهُمَا بِقُدْرَتِهِ",
                "All praise is for Allah Who created night and day by His strength and distinguished between them by His power.",
                "سب تعریف اللہ کے لیے جس نے رات اور دن کو اپنی قوت سے پیدا کیا۔",
                "ستایش خدایی را که شب و روز را به نیرویش آفرید.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("7. When Faced with Worrisome Tasks",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِهِ، وَافْتَحْ لِي بِرَحْمَتِكَ بَابَ فَرَجِكَ… وَسَبِّبْ لِي أَسْبَابًا تُفَرِّجُ بِهَا هَمِّي",
                "O Allah, bless Muhammad and his family, open for me by Your mercy the door of Your relief… and arrange causes that remove my worry.",
                "اے اللہ محمد و آل پر درود بھیج اور اپنی رحمت سے فرج کا دروازہ کھول…",
                "خدایا بر محمد و آل درود فرست و به رحمتت در فرج بگشا…",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("8. Seeking Refuge from Evils",
                "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ هَيَجَانِ الْحِرْصِ، وَسَوْرَةِ الْغَضَبِ، وَغَلَبَةِ الْحَسَدِ",
                "O Allah, I seek refuge in You from the surge of greed, the intensity of anger, and the overpowering of envy.",
                "اے اللہ میں تیری پناہ مانگتا ہوں حرص کے جوش، غصے کی شدت اور حسد کے غلبے سے۔",
                "خدایا به تو پناه می‌برم از جوش حرص و شدت خشم و غلبه حسد.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("9. Yearning for Forgiveness",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِهِ، وَصَيِّرْنَا إِلَىٰ مَحْبُوبِكَ مِنَ التَّوْبَةِ، وَأَزِلْنَا إِلَيْكَ بِخَالِصٍ مِنَ الْإِنَابَةِ",
                "O Allah, bless Muhammad and his family, turn us toward the repentance You love, and move us to You with sincere turning.",
                "اے اللہ ہمیں تیری پسندیدہ توبہ کی طرف پھیر…",
                "خدایا ما را به توبه محبوبت بازگردان…",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("10. Seeking Asylum with Allah",
                "اللَّهُمَّ احْمِلْنِي عَلَىٰ عَفْوِكَ، وَلَا تَحْمِلْنِي عَلَىٰ عَدْلِكَ",
                "O Allah, deal with me by Your pardon, and do not deal with me by Your justice alone.",
                "اے اللہ مجھ سے معافی سے پیش آ، صرف عدل سے نہیں۔",
                "خدایا با عفوت با من رفتار کن، نه تنها با عدلت.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("11. For Good Outcomes",
                "يَا مَنْ ذِكْرُهُ شَرَفٌ لِلذَّاكِرِينَ، وَيَا مَنْ شُكْرُهُ فَوْزٌ لِلشَّاكِرِينَ",
                "O He Whose remembrance is an honour for those who remember, and Whose thanks is success for those who thank.",
                "اے وہ جس کا ذکر ذاکرین کے لیے عزت ہے…",
                "ای آنکه ذکرش شرف ذاکران است…",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("12. Confession and Seeking Repentance",
                "اللَّهُمَّ إِنَّهُ يَغْشَىٰ وَيَعْلُو وَيَغْلِبُ… أَنِّي أَتَقَرَّبُ إِلَيْكَ بِالْمُحَمَّدِيَّةِ الرَّفِيعَةِ",
                "O Allah… I draw near to You through the elevated Muhammadan path, and the Alawi rank.",
                "اے اللہ… میں تیرے قریب محمدی بلندی کے وسیلے سے آتا ہوں۔",
                "خدایا… با مرتبه بلند محمدی به تو تقرب می‌جویم.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("13. Asking for Covering of Defects",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِهِ، وَاسْتُرْ عَلَيَّ بِسِتْرِكَ، وَاجْعَلْ عِصْمَتَكَ مِنِّي بَدَلًا",
                "O Allah, bless Muhammad and his family, cover me with Your covering, and make Your protection a substitute for me.",
                "اے اللہ مجھے اپنے پردے سے ڈھانپ…",
                "خدایا مرا با پرده‌ات بپوشان…",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("15. When Sick",
                "اللَّهُمَّ لَكَ الْحَمْدُ عَلَىٰ مَا لَمْ أَزَلْ أَتَصَرَّفُ فِيهِ مِنْ سَلَامَةِ بَدَنِي",
                "O Allah, to You is praise for the soundness of body I have continued to enjoy… and for the health in my limbs.",
                "اے اللہ تیری تعریف ہے جسم کی سلامتی پر…",
                "خدایا ستایش تو را برای سلامت بدن…",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("20. Noble Moral Traits",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِهِ، وَبَلِّغْ بِإِيمَانِي أَكْمَلَ الْإِيمَانِ، وَاجْعَلْ يَقِينِي أَفْضَلَ الْيَقِينِ",
                "O Allah, bless Muhammad and his family, make my faith the most complete, and my certainty the best certainty.",
                "اے اللہ میرے ایمان کو کامل اور یقین کو افضل بنا۔",
                "خدایا ایمانم را کامل‌ترین و یقینم را برترین گردان.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("21. When Something Makes Him Sorrowful",
                "يَا فَارِجَ الْهَمِّ، وَيَا كَاشِفَ الْغَمِّ… صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِهِ، وَافْرُجْ هَمِّي، وَاكْشِفْ غَمِّي",
                "O Reliever of worry, O Remover of grief… bless Muhammad and his family, relieve my worry and remove my grief.",
                "اے غم دور کرنے والے… میرا غم دور فرما۔",
                "ای برطرف‌کننده اندوه… اندوهم را بزدا.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("22. In Hardship and Distress",
                "اللَّهُمَّ إِنَّكَ كَلَّفْتَنِي مِنْ نَفْسِي مَا أَنْتَ أَمْلَكُ بِهِ مِنِّي… فَصَلِّ عَلَىٰ مُحَمَّدٍ وَآلِهِ، وَأَعِنِّي",
                "O Allah, You have charged me concerning myself with that over which You have more power than I… so bless Muhammad and his family and help me.",
                "اے اللہ تو نے مجھے اپنے نفس کے بارے میں مکلف کیا… پس میری مدد فرما۔",
                "خدایا مرا درباره نفسم مکلف کردی… پس یاری‌ام کن.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("23. For Well-Being",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِهِ، وَأَلْبِسْنِي عَافِيَتَكَ، وَجَلِّلْنِي بِعَافِيَتِكَ",
                "O Allah, bless Muhammad and his family, clothe me in Your well-being, and wrap me in Your well-being.",
                "اے اللہ مجھے اپنی عافیت کا لباس پہنا۔",
                "خدایا جامه عافیتت بر من بپوشان.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("27. For the People of the Frontiers",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِهِ، وَحَصِّنْ ثُغُورَ الْمُسْلِمِينَ بِعِزَّتِكَ",
                "O Allah, bless Muhammad and his family, and fortify the frontiers of the Muslims with Your might.",
                "اے اللہ مسلمانوں کی سرحدوں کو اپنی عزت سے مضبوط کر۔",
                "خدایا مرزهای مسلمانان را به عزتت استوار کن.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("32. After Confessing Shortcomings",
                "اللَّهُمَّ إِنَّهُ يَغْشَانِي وَيَغْلِبُنِي… وَقَدْ أَتَيْتُكَ مِنَ الذُّنُوبِ بِمَا قَدْ أَحْصَيْتَهُ عَلَيَّ",
                "O Allah… I have come to You with sins which You have already counted against me — so forgive me.",
                "اے اللہ… میں گناہوں کے ساتھ آیا ہوں جو تو نے گن رکھے ہیں — پس مجھے بخش دے۔",
                "خدایا… با گناهانی آمده‌ام که بر من شمرده‌ای — پس بیامرز.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("45. Farewell to Ramadan",
                "اللَّهُمَّ يَا مَنْ لَا يَرْغَبُ فِي الْجَزَاءِ، وَيَا مَنْ لَا يَنْدَمُ عَلَىٰ الْعَطَاءِ… هَٰذَا شَهْرُ رَمَضَانَ الَّذِي أَنْزَلْتَ فِيهِ الْقُرْآنَ",
                "O Allah, O He Who does not seek a return for gift… this is the month of Ramadan in which You sent down the Quran.",
                "اے اللہ… یہ رمضان کا مہینہ ہے جس میں تو نے قرآن نازل فرمایا۔",
                "خدایا… این ماه رمضان است که در آن قرآن را فرو فرستادی.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("47. On the Day of Arafah",
                "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ… اللَّهُمَّ لَكَ الْحَمْدُ بَدِيعَ السَّمَاوَاتِ وَالْأَرْضِ",
                "All praise is for Allah, Lord of the worlds… O Allah, to You is praise, Originator of the heavens and the earth.",
                "سب تعریف اللہ رب العالمین کے لیے… آسمان و زمین کے موجد!",
                "ستایش پروردگار جهانیان را… آفریننده آسمان‌ها و زمین!",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        list.add(dua("54. Removing Worries (closing munajat style)",
                "يَا مَنْ تُحَلُّ بِهِ عُقَدُ الْمَكَارِهِ، وَيَا مَنْ يُفْثَأُ بِهِ حَدُّ الشَّدَائِدِ",
                "O He by Whom the knots of disliked things are undone, and by Whom the edge of hardships is blunted.",
                "اے وہ جس سے ناپسندیدہ گرہیں کھلتی ہیں اور سختیوں کی تیزی کند پڑتی ہے۔",
                "ای آنکه به او گره‌های ناخوشی گشوده شود و تیزی سختی‌ها شکسته گردد.",
                src, SectTag.BOTH, ContentCategory.SAHIFA));
        return list;
    }

    private static List<HadithEntity> buildAhadithFallback() {
        List<HadithEntity> list = new ArrayList<>();
        list.add(hadith("إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ",
                "Actions are but by intention.",
                "اعمال کا دارومدار نیتوں پر ہے۔",
                "اعمال جز به نیت‌ها نیست.",
                "Bukhari & Muslim", SectTag.SUNNI));
        list.add(hadith("أَنَا مَدِينَةُ الْعِلْمِ وَعَلِيٌّ بَابُهَا",
                "I am the city of knowledge and Ali is its gate.",
                "میں علم کا شہر ہوں اور علی اس کا دروازہ ہیں۔",
                "من شهر علمم و علی دروازه آن است.",
                "Shia sources", SectTag.SHIA));
        return list;
    }

    private static List<BookEntity> buildBooks() {
        List<BookEntity> list = new ArrayList<>();
        String soon = "soon://offline"; // placeholder — UI shows Coming soon

        list.add(new BookEntity("Mafatih al-Jinan", "Sheikh Abbas Qumi", soon, SectTag.SHIA));
        list.add(new BookEntity("Sahifa Sajjadiya", "Imam Ali Zayn al-Abidin (a)", soon, SectTag.BOTH));
        list.add(new BookEntity("Nahj al-Balagha", "Compiled by Sharif al-Radi", soon, SectTag.SHIA));
        list.add(new BookEntity("Al-Kafi (selections)", "Al-Kulayni", soon, SectTag.SHIA));
        list.add(new BookEntity("Bihar al-Anwar (selections)", "Allama Majlisi", soon, SectTag.SHIA));
        list.add(new BookEntity("Tuhaf al-Uqul", "Ibn Shu'ba al-Harrani", soon, SectTag.SHIA));
        list.add(new BookEntity("Kitab al-Irshad", "Sheikh al-Mufid", soon, SectTag.SHIA));
        list.add(new BookEntity("Amali of Saduq (selections)", "Sheikh Saduq", soon, SectTag.SHIA));
        list.add(new BookEntity("Mizan al-Hikmah (selections)", "Muhammadi Reyshahri", soon, SectTag.SHIA));
        list.add(new BookEntity("Ziyarat Ashura Commentary", "Traditional", soon, SectTag.SHIA));

        list.add(new BookEntity("Hisnul Muslim", "Sa'id bin Ali bin Wahf Al-Qahtani", soon, SectTag.SUNNI));
        list.add(new BookEntity("Riyad as-Salihin", "Imam an-Nawawi", soon, SectTag.SUNNI));
        list.add(new BookEntity("Sahih al-Bukhari (selections)", "Imam al-Bukhari", soon, SectTag.SUNNI));
        list.add(new BookEntity("Sahih Muslim (selections)", "Imam Muslim", soon, SectTag.SUNNI));
        list.add(new BookEntity("Forty Hadith of Nawawi", "Imam an-Nawawi", soon, SectTag.SUNNI));
        list.add(new BookEntity("Fortress of the Muslim", "Al-Qahtani", soon, SectTag.SUNNI));
        list.add(new BookEntity("Tafsir Ibn Kathir (selections)", "Ibn Kathir", soon, SectTag.SUNNI));
        list.add(new BookEntity("Al-Adab al-Mufrad", "Imam al-Bukhari", soon, SectTag.SUNNI));
        list.add(new BookEntity("Shama'il Muhammadiyah", "Imam at-Tirmidhi", soon, SectTag.SUNNI));
        list.add(new BookEntity("Bulugh al-Maram (selections)", "Ibn Hajar", soon, SectTag.SUNNI));

        list.add(new BookEntity("The Holy Quran", "Arabic + translations in app", soon, SectTag.BOTH));
        list.add(new BookEntity("Stories of the Prophets", "Ibn Kathir / Traditional", soon, SectTag.BOTH));
        return list;
    }
}
