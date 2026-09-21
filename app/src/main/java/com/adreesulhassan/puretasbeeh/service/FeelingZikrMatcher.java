package com.adreesulhassan.puretasbeeh.service;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Offline feeling → zikr / short dua / Quran ayah matcher (no network, no Gemini).
 */
public final class FeelingZikrMatcher {

    public static final class Suggestion {
        @NonNull
        public final String arabic;
        @NonNull
        public final String meaning;
        public final int recommendedCount;
        @NonNull
        public final String kind; // zikr | dua | ayah

        public Suggestion(@NonNull String arabic,
                          @NonNull String meaning,
                          int recommendedCount,
                          @NonNull String kind) {
            this.arabic = arabic;
            this.meaning = meaning;
            this.recommendedCount = recommendedCount > 0 ? recommendedCount : 33;
            this.kind = kind;
        }
    }

    private static final class Entry {
        final String[] keywords;
        final Suggestion[] options;

        Entry(String[] keywords, Suggestion[] options) {
            this.keywords = keywords;
            this.options = options;
        }
    }

    private static final Entry[] ENTRIES = {
            new Entry(new String[]{
                    "anxious", "anxiety", "worry", "worried", "fear", "scared", "nervous", "stress",
                    "پریشان", "فکر", "ڈر", "خوف", "تناؤ",
                    "قلق", "خوف", "توتر", "قلقان",
                    "نگران", "اضطراب", "ترس", "استرس"
            }, new Suggestion[]{
                    new Suggestion("حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                            "Allah is sufficient for us, and He is the best Disposer of affairs. (3:173)",
                            33, "ayah"),
                    new Suggestion("اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ",
                            "O Allah, I seek refuge in You from worry and grief.",
                            33, "dua"),
                    new Suggestion("لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                            "There is no power and no strength except with Allah.",
                            100, "zikr"),
            }),
            new Entry(new String[]{
                    "sad", "grief", "depressed", "lonely", "hurt", "cry", "tears", "heartbroken",
                    "غم", "اداس", "تنہا", "رنج",
                    "حزن", "حزين", "وحدة",
                    "غمگین", "ناراحت", "تنها"
            }, new Suggestion[]{
                    new Suggestion("إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ",
                            "Indeed we belong to Allah, and indeed to Him we will return. (2:156)",
                            33, "ayah"),
                    new Suggestion("اللَّهُمَّ ارْحَمْنِي وَآنِسْ وَحْشَتِي",
                            "O Allah, have mercy on me and comfort my loneliness.",
                            33, "dua"),
                    new Suggestion("يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ",
                            "O Ever-Living, O Sustainer, by Your mercy I seek help.",
                            33, "zikr"),
            }),
            new Entry(new String[]{
                    "grateful", "thankful", "gratitude", "blessed", "happy", "joy", "alhamdulillah",
                    "شکر", "ممنون", "خوش", "شکرگزار",
                    "شكر", "سعيد", "فرح",
                    "سپاس", "متشکر", "خوشحال"
            }, new Suggestion[]{
                    new Suggestion("الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                            "All praise is for Allah, Lord of the worlds. (1:2)",
                            33, "ayah"),
                    new Suggestion("اللَّهُمَّ لَكَ الْحَمْدُ كَمَا يَنْبَغِي لِجَلَالِ وَجْهِكَ",
                            "O Allah, to You belongs praise as befits the majesty of Your Face.",
                            33, "dua"),
                    new Suggestion("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                            "Glory be to Allah and praise be to Him.",
                            100, "zikr"),
            }),
            new Entry(new String[]{
                    "peace", "calm", "tranquil", "serene", "relax", "seek peace", "seeking peace",
                    "سکون", "امن", "آرام",
                    "سلام", "طمأنينة", "هدوء",
                    "آرامش", "صلح"
            }, new Suggestion[]{
                    new Suggestion("أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                            "Unquestionably, by the remembrance of Allah hearts are assured. (13:28)",
                            33, "ayah"),
                    new Suggestion("اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ",
                            "O Allah, You are Peace and from You is peace.",
                            33, "dua"),
                    new Suggestion("سُبْحَانَ اللَّهِ",
                            "Glory be to Allah.",
                            33, "zikr"),
            }),
            new Entry(new String[]{
                    "angry", "anger", "rage", "frustrated", "annoyed", "hate",
                    "غصہ", "غصے", "ناراض",
                    "غضب", "غضبان",
                    "عصبانی", "خشم"
            }, new Suggestion[]{
                    new Suggestion("أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ",
                            "I seek refuge in Allah from the accursed Satan.",
                            33, "zikr"),
                    new Suggestion("اللَّهُمَّ أَذْهِبْ غَيْظَ قَلْبِي",
                            "O Allah, remove the rage from my heart.",
                            33, "dua"),
                    new Suggestion("وَالْكَاظِمِينَ الْغَيْظَ وَالْعَافِينَ عَنِ النَّاسِ",
                            "…who restrain anger and pardon people… (3:134)",
                            33, "ayah"),
            }),
            new Entry(new String[]{
                    "forgive", "forgiveness", "repent", "sin", "guilt", "sorry", "istighfar",
                    "معافی", "توبہ", "گناہ",
                    "استغفر", "توبة", "ذنب",
                    "آمرزش", "توبه"
            }, new Suggestion[]{
                    new Suggestion("أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ",
                            "I seek forgiveness of Allah, the Magnificent.",
                            100, "zikr"),
                    new Suggestion("رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ",
                            "My Lord, forgive me and accept my repentance.",
                            33, "dua"),
                    new Suggestion("وَاسْتَغْفِرُوا اللَّهَ إِنَّ اللَّهَ غَفُورٌ رَحِيمٌ",
                            "And seek forgiveness of Allah. Indeed, Allah is Forgiving and Merciful. (73:20)",
                            33, "ayah"),
            }),
            new Entry(new String[]{
                    "hope", "hopeful", "optimistic", "trust", "tawakkul", "rely",
                    "امید", "بھروسہ",
                    "أمل", "توكل",
                    "امیدوار", "توکل"
            }, new Suggestion[]{
                    new Suggestion("حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ",
                            "Allah is sufficient for me; there is no deity except Him. On Him I rely. (9:129)",
                            33, "ayah"),
                    new Suggestion("اللَّهُمَّ إِنِّي أَسْأَلُكَ حُسْنَ الْخَاتِمَةِ",
                            "O Allah, I ask You for a good ending.",
                            33, "dua"),
                    new Suggestion("يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
                            "O Turner of hearts, keep my heart firm upon Your religion.",
                            33, "zikr"),
            }),
            new Entry(new String[]{
                    "sick", "ill", "pain", "health", "heal", "fever", "tired", "weak",
                    "بیمار", "درد", "تھک",
                    "مرض", "ألم", "تعب",
                    "بیمار", "درد", "خسته"
            }, new Suggestion[]{
                    new Suggestion("اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَأْسَ اشْفِ أَنْتَ الشَّافِي",
                            "O Allah, Lord of mankind, remove the harm and heal; You are the Healer.",
                            33, "dua"),
                    new Suggestion("وَإِذَا مَرِضْتُ فَهُوَ يَشْفِينِ",
                            "And when I am ill, it is He who cures me. (26:80)",
                            33, "ayah"),
                    new Suggestion("يَا شَافِي اشْفِنِي",
                            "O Healer, heal me.",
                            33, "zikr"),
            }),
            new Entry(new String[]{
                    "guidance", "confused", "lost", "decision", "help", "guide",
                    "رہنمائی", "مشکل", "مدد",
                    "هداية", "حيرة", "مساعدة",
                    "هدایت", "گیج", "کمک"
            }, new Suggestion[]{
                    new Suggestion("اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
                            "Guide us to the straight path. (1:6)",
                            33, "ayah"),
                    new Suggestion("اللَّهُمَّ اهْدِنِي وَسَدِّدْنِي",
                            "O Allah, guide me and set me aright.",
                            33, "dua"),
                    new Suggestion("يَا هَادِي اهْدِنِي",
                            "O Guide, guide me.",
                            33, "zikr"),
            }),
            new Entry(new String[]{
                    "love", "loving", "mercy", "kind", "compassion",
                    "محبت", "رحم",
                    "حب", "رحمة",
                    "عشق", "مهر"
            }, new Suggestion[]{
                    new Suggestion("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            "In the name of Allah, the Most Gracious, the Most Merciful.",
                            33, "ayah"),
                    new Suggestion("اللَّهُمَّ إِنِّي أَسْأَلُكَ حُبَّكَ وَحُبَّ مَنْ يُحِبُّكَ",
                            "O Allah, I ask You for Your love and the love of those who love You.",
                            33, "dua"),
                    new Suggestion("يَا وَدُودُ",
                            "O Most Loving.",
                            33, "zikr"),
            }),
    };

    private static final Suggestion[] FALLBACK = {
            new Suggestion("سُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ وَلَا إِلَهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ",
                    "Glory be to Allah, praise be to Allah, there is no god but Allah, and Allah is the Greatest.",
                    33, "zikr"),
            new Suggestion("لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
                    "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers. (21:87)",
                    33, "ayah"),
            new Suggestion("رَبِّي زِدْنِي عِلْمًا",
                    "My Lord, increase me in knowledge. (20:114)",
                    33, "ayah"),
            new Suggestion("اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ",
                    "O Allah, bless Muhammad and the family of Muhammad.",
                    34, "dua"),
    };

    private final Random random = new Random();

    @NonNull
    public Suggestion suggest(@NonNull String feeling) {
        String q = feeling.toLowerCase(Locale.ROOT).trim();
        if (q.isEmpty()) {
            return FALLBACK[random.nextInt(FALLBACK.length)];
        }

        List<Suggestion> hits = new ArrayList<>();
        for (Entry entry : ENTRIES) {
            for (String kw : entry.keywords) {
                if (q.contains(kw.toLowerCase(Locale.ROOT))) {
                    for (Suggestion s : entry.options) {
                        hits.add(s);
                    }
                    break;
                }
            }
        }
        if (!hits.isEmpty()) {
            return hits.get(random.nextInt(hits.size()));
        }
        return FALLBACK[random.nextInt(FALLBACK.length)];
    }
}
