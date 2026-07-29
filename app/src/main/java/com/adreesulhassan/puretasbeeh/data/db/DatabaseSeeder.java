package com.adreesulhassan.puretasbeeh.data.db;

import com.adreesulhassan.puretasbeeh.data.entity.BookEntity;
import com.adreesulhassan.puretasbeeh.data.entity.ContentCategory;
import com.adreesulhassan.puretasbeeh.data.entity.ContentEntity;
import com.adreesulhassan.puretasbeeh.data.entity.HadithEntity;
import com.adreesulhassan.puretasbeeh.data.entity.SectTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Seed data for first Room create — Shia (Sahifa/Mafatih-style),
 * Sunni (Hisnul Muslim-style), universal books & ahadith.
 */
public final class DatabaseSeeder {

    private DatabaseSeeder() {
    }

    public static void seed(AppDatabase db) {
        if (db.contentDao().count() == 0) {
            db.contentDao().insertAll(buildContent());
        }
        if (db.bookDao().count() == 0) {
            db.bookDao().insertAll(buildBooks());
        }
        if (db.hadithDao().count() == 0) {
            db.hadithDao().insertAll(buildAhadith());
        }
    }

    private static List<ContentEntity> buildContent() {
        List<ContentEntity> list = new ArrayList<>();

        // —— SHIA / Jafriya (Sahifa Sajjadiya & Mafatih style) ——
        list.add(new ContentEntity(
                "Dua before Namaz",
                "اللَّهُمَّ إِنِّي أُقَدِّمُ إِلَيْكَ مُحَمَّداً نَبِيَّكَ",
                "O Allah, I present before You Muhammad, Your Prophet… (before prayer)",
                SectTag.SHIA,
                ContentCategory.NAMAZ));
        list.add(new ContentEntity(
                "Tasbih after Namaz",
                "سُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ وَلَا إِلَٰهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ",
                "Glory be to Allah, praise be to Allah, there is no god but Allah, and Allah is Greatest.",
                SectTag.SHIA,
                ContentCategory.NAMAZ));
        list.add(new ContentEntity(
                "Dua Kumayl (excerpt)",
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ",
                "O Allah, I ask You by Your mercy which encompasses all things…",
                SectTag.SHIA,
                ContentCategory.SPECIAL));
        list.add(new ContentEntity(
                "Munajat of Imam Sajjad (excerpt)",
                "إِلَٰهِي أَنَا الْفَقِيرُ فِي غِنَايَ",
                "My God, I am the poor one in my riches… (Sahifa Sajjadiya)",
                SectTag.SHIA,
                ContentCategory.SPECIAL));
        list.add(new ContentEntity(
                "Dua for the Month of Ramadan",
                "اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالْأَمْنِ وَالْإِيمَانِ",
                "O Allah, let it rise over us with security and faith…",
                SectTag.SHIA,
                ContentCategory.MONTHS));
        list.add(new ContentEntity(
                "Dua for Rajab",
                "يَا مَنْ أَرْجُوهُ لِكُلِّ خَيْرٍ",
                "O He Whom I hope for every good… (Rajab)",
                SectTag.SHIA,
                ContentCategory.MONTHS));
        list.add(new ContentEntity(
                "Morning Dua (Shia)",
                "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ",
                "We have entered the morning and the dominion belongs to Allah…",
                SectTag.SHIA,
                ContentCategory.DAILY));

        // —— SUNNI / Hanfiya (Hisnul Muslim style) ——
        list.add(new ContentEntity(
                "Dua opening Salah",
                "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ",
                "Glory is to You, O Allah, and praise; blessed is Your Name… (opening of prayer)",
                SectTag.SUNNI,
                ContentCategory.NAMAZ));
        list.add(new ContentEntity(
                "Dua after Tashahhud",
                "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عَذَابِ جَهَنَّمَ",
                "O Allah, I seek refuge in You from the punishment of Hellfire…",
                SectTag.SUNNI,
                ContentCategory.NAMAZ));
        list.add(new ContentEntity(
                "Dua for distress (Hisnul Muslim)",
                "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
                "There is no god but You; glory be to You. Indeed I was among the wrongdoers.",
                SectTag.SUNNI,
                ContentCategory.SPECIAL));
        list.add(new ContentEntity(
                "Dua for protection",
                "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ",
                "In the name of Allah, with Whose name nothing on earth or in heaven can cause harm…",
                SectTag.SUNNI,
                ContentCategory.SPECIAL));
        list.add(new ContentEntity(
                "Sighting the new moon",
                "اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالْيُمْنِ وَالْإِيمَانِ",
                "O Allah, bring it over us with blessing and faith…",
                SectTag.SUNNI,
                ContentCategory.MONTHS));
        list.add(new ContentEntity(
                "Dua for Laylatul Qadr",
                "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
                "O Allah, You are Pardoning and love to pardon, so pardon me.",
                SectTag.SUNNI,
                ContentCategory.MONTHS));
        list.add(new ContentEntity(
                "Morning remembrance",
                "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ",
                "We have reached the morning and at this very time the dominion belongs to Allah…",
                SectTag.SUNNI,
                ContentCategory.DAILY));

        // —— BOTH ——
        list.add(new ContentEntity(
                "Istighfar",
                "أَسْتَغْفِرُ اللَّهَ رَبِّي مِنْ كُلِّ ذَنْبٍ وَأَتُوبُ إِلَيْهِ",
                "I seek forgiveness from Allah, my Lord, from every sin, and I turn to Him in repentance.",
                SectTag.BOTH,
                ContentCategory.DAILY));
        list.add(new ContentEntity(
                "Salawat",
                "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَآلِ مُحَمَّدٍ",
                "O Allah, send blessings upon Muhammad and the family of Muhammad.",
                SectTag.BOTH,
                ContentCategory.SPECIAL));

        return list;
    }

    private static List<BookEntity> buildBooks() {
        List<BookEntity> books = new ArrayList<>();
        // Public-domain / openly hosted sample PDFs for DownloadManager testing
        books.add(new BookEntity(
                "The Noble Quran (English)",
                "Translation — sample open text",
                "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"));
        books.add(new BookEntity(
                "Seerah Overview (Sample)",
                "Classic Seerah digest",
                "https://www.africau.edu/images/default/sample.pdf"));
        books.add(new BookEntity(
                "Tafseer Sample Volume",
                "Open educational excerpt",
                "https://www.adobe.com/support/products/enterprise/knowledgecenter/media/c4611_sample_explain.pdf"));
        books.add(new BookEntity(
                "Hisnul Muslim (Reference Link)",
                "Fortress of the Muslim — sample file",
                "https://www.learningcontainer.com/wp-content/uploads/2019/09/sample-pdf-file.pdf"));
        return books;
    }

    private static List<HadithEntity> buildAhadith() {
        List<HadithEntity> list = new ArrayList<>();
        list.add(new HadithEntity(
                "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ",
                "Actions are but by intentions.",
                "Bukhari & Muslim",
                SectTag.SUNNI));
        list.add(new HadithEntity(
                "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
                "The best of you are those who learn the Quran and teach it.",
                "Bukhari",
                SectTag.SUNNI));
        list.add(new HadithEntity(
                "أَنَا مَدِينَةُ الْعِلْمِ وَعَلِيٌّ بَابُهَا",
                "I am the city of knowledge and Ali is its gate.",
                "Narrated in Shia sources",
                SectTag.SHIA));
        list.add(new HadithEntity(
                "حُسَيْنٌ مِنِّي وَأَنَا مِنْ حُسَيْنٍ",
                "Husayn is from me and I am from Husayn.",
                "Tirmidhi / Shia collections",
                SectTag.BOTH));
        list.add(new HadithEntity(
                "مَنْ عَرَفَ نَفْسَهُ فَقَدْ عَرَفَ رَبَّهُ",
                "Whoever knows himself has known his Lord.",
                "Attributed in various collections",
                SectTag.BOTH));
        list.add(new HadithEntity(
                "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَىٰ كُلِّ مُسْلِمٍ",
                "Seeking knowledge is an obligation upon every Muslim.",
                "Ibn Majah / widely narrated",
                SectTag.BOTH));
        return list;
    }
}
