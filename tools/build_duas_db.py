#!/usr/bin/env python3
"""Build assets/databases/duas.db from Hisnul Muslim + curated full Shia/Sahifa pack.
Translates EN -> UR/FA via Google Translate (no Gemini tokens).
"""
import json
import os
import sqlite3
import time

from deep_translator import GoogleTranslator

ROOT = os.path.dirname(os.path.abspath(__file__))
DATA = os.path.join(ROOT, "data")
OUT_DB = os.path.join(ROOT, "..", "app", "src", "main", "assets", "databases", "duas.db")
PROGRESS = os.path.join(DATA, "duas_tr_progress.json")
HISNUL = os.path.join(DATA, "hisnul.json")

# Map hisnul category names -> our ContentCategory
CAT_MAP = {
    "Waking & Sleeping": "DAILY",
    "Morning & Evening": "DAILY",
    "Home & Family": "DAILY",
    "Food & Drink": "DAILY",
    "Clothing": "DAILY",
    "Toilet": "DAILY",
    "Mosque": "NAMAZ",
    "Prayer": "NAMAZ",
    "Adhan & Iqamah": "NAMAZ",
    "Travel": "SPECIAL",
    "Protection": "SPECIAL",
    "Illness & Death": "SPECIAL",
    "Joy & Distress": "SPECIAL",
    "Forgiveness & Repentance": "SPECIAL",
    "Praise & Thankfulness": "SPECIAL",
}


def map_cat(name: str) -> str:
    for k, v in CAT_MAP.items():
        if k.lower() in name.lower():
            return v
    return "DAILY"


def chunk(s: str, n: int = 4500):
    s = (s or "").strip()
    if not s:
        return []
    if len(s) <= n:
        return [s]
    out = []
    while s:
        out.append(s[:n])
        s = s[n:]
    return out


def tr(translator, text: str) -> str:
    outs = []
    for p in chunk(text):
        for attempt in range(5):
            try:
                outs.append(translator.translate(p))
                time.sleep(0.12)
                break
            except Exception:
                time.sleep(1.5 * (attempt + 1))
                if attempt == 4:
                    raise
    return " ".join(outs)


def load_hisnul():
    items = []
    with open(HISNUL, encoding="utf-8") as f:
        data = json.load(f)
    for seg in data.get("segments", []):
        for cat in seg.get("categories", []):
            cname = cat.get("category_name", "Daily")
            category = map_cat(cname)
            for title in cat.get("titles", []):
                tname = title.get("title_name", "Dua")
                duas = title.get("duas", [])
                if not duas:
                    continue
                # Merge all duas under a title into one full page when multiple short ones
                ar_parts, en_parts, sources = [], [], []
                for d in duas:
                    ar = (d.get("arabic") or "").strip()
                    en = (d.get("translation") or "").strip()
                    src = (d.get("source") or "Hisnul Muslim").strip()
                    if ar:
                        ar_parts.append(ar)
                    if en:
                        en_parts.append(en)
                    if src:
                        sources.append(src)
                if not ar_parts:
                    continue
                items.append({
                    "title": tname,
                    "arabic": "\n\n".join(ar_parts),
                    "en": "\n\n".join(en_parts) if en_parts else tname,
                    "source": "Hisnul Muslim" + (f" · {sources[0]}" if sources else ""),
                    # Daily/Namaz adhkar are shared practice; expose to both fiqh tabs.
                    "sect": "BOTH" if category in ("DAILY", "NAMAZ") else "SUNNI",
                    "category": category,
                })
    return items


def curated_shia_extra():
    """Fuller page-length Shia / Sahifa / Mafatih texts (public traditional duas)."""
    return [
        {
            "title": "Dua Kumayl (full)",
            "arabic": """بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ
اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ، وَبِقُوَّتِكَ الَّتِي قَهَرْتَ بِهَا كُلَّ شَيْءٍ، وَخَضَعَ لَهَا كُلُّ شَيْءٍ، وَذَلَّ لَهَا كُلُّ شَيْءٍ، وَبِجَبَرُوتِكَ الَّتِي غَلَبْتَ بِهَا كُلَّ شَيْءٍ، وَبِعِزَّتِكَ الَّتِي لَا يَقُومُ لَهَا شَيْءٌ، وَبِعَظَمَتِكَ الَّتِي مَلَأَتْ كُلَّ شَيْءٍ، وَبِسُلْطَانِكَ الَّذِي عَلَا كُلَّ شَيْءٍ، وَبِوَجْهِكَ الْبَاقِي بَعْدَ فَنَاءِ كُلِّ شَيْءٍ، وَبِأَسْمَائِكَ الَّتِي مَلَأَتْ أَرْكَانَ كُلِّ شَيْءٍ، وَبِعِلْمِكَ الَّذِي أَحَاطَ بِكُلِّ شَيْءٍ، وَبِنُورِ وَجْهِكَ الَّذِي أَضَاءَ لَهُ كُلُّ شَيْءٍ.
يَا نُورُ يَا قُدُّوسُ يَا أَوَّلَ الْأَوَّلِينَ وَيَا آخِرَ الْآخِرِينَ.
اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تَهْتِكُ الْعِصَمَ، اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تُنْزِلُ النِّقَمَ، اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تُغَيِّرُ النِّعَمَ، اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تَحْبِسُ الدُّعَاءَ، اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تُنْزِلُ الْبَلَاءَ، اللَّهُمَّ اغْفِرْ لِي كُلَّ ذَنْبٍ أَذْنَبْتُهُ، وَكُلَّ خَطِيئَةٍ أَخْطَأْتُهَا.
اللَّهُمَّ إِنِّي أَتَقَرَّبُ إِلَيْكَ بِذِكْرِكَ، وَأَسْتَشْفِعُ بِكَ إِلَى نَفْسِكَ، وَأَسْأَلُكَ بِجُودِكَ أَنْ تُدْنِيَنِي مِنْ قُرْبِكَ، وَأَنْ تُوزِعَنِي شُكْرَكَ، وَأَنْ تُلْهِمَنِي ذِكْرَكَ.
يَا كَرِيمُ أَسْأَلُكَ بِكَرَمِكَ أَنْ تَجْعَلَنِي مِنْ أَهْلِ كَرَامَتِكَ، وَأَنْ تَغْفِرَ لِي مَا تَقَدَّمَ مِنْ ذَنْبِي وَمَا تَأَخَّرَ، وَأَنْ تَرْحَمَنِي رَحْمَةً تُغْنِينِي بِهَا عَنْ رَحْمَةِ مَنْ سِوَاكَ.""",
            "en": """In the name of Allah, the Most Gracious, the Most Merciful.
O Allah, I ask You by Your mercy which encompasses all things; by Your strength with which You dominate all things, before which all things are humble and lowly; by Your might with which You overcome all things; by Your glory before which nothing stands; by Your greatness which fills all things; by Your authority which towers over all things; by Your Face which remains after all things perish; by Your Names which fill the pillars of all things; by Your knowledge which encompasses all things; and by the light of Your Face which illuminates all things.
O Light, O Holy, O First of the first and Last of the last.
O Allah, forgive me the sins that tear away safeguards; forgive me the sins that bring down calamities; forgive me the sins that change blessings; forgive me the sins that hold back prayer; forgive me the sins that bring down affliction; forgive me every sin I have committed and every mistake I have made.
O Allah, I draw near to You by Your remembrance, seek intercession through You to Yourself, and ask You by Your generosity to draw me near to Your proximity, to allot me gratitude to You, and to inspire me with Your remembrance.
O Generous One, I ask You by Your generosity to make me among the people of Your honor, to forgive what of my sin has gone before and what remains, and to have mercy on me with a mercy that makes me needless of the mercy of anyone besides You.""",
            "source": "Mafatih al-Jinan · Dua Kumayl",
            "sect": "SHIA",
            "category": "SPECIAL",
        },
        {
            "title": "Dua Tawassul",
            "arabic": """اللَّهُمَّ إِنِّي أَسْأَلُكَ وَأَتَوَجَّهُ إِلَيْكَ بِنَبِيِّكَ نَبِيِّ الرَّحْمَةِ مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَآلِهِ، يَا أَبَا الْقَاسِمِ يَا رَسُولَ اللَّهِ يَا إِمَامَ الرَّحْمَةِ يَا شَفِيعَ الْأُمَّةِ يَا حَبِيبَ اللَّهِ، إِنَّا تَوَجَّهْنَا وَاسْتَشْفَعْنَا وَتَوَسَّلْنَا بِكَ إِلَى اللَّهِ، وَقَدَّمْنَاكَ بَيْنَ يَدَيْ حَاجَاتِنَا، يَا وَجِيهًا عِنْدَ اللَّهِ اشْفَعْ لَنَا عِنْدَ اللَّهِ.
يَا أَبَا الْحَسَنِ يَا أَمِيرَ الْمُؤْمِنِينَ يَا عَلِيَّ بْنَ أَبِي طَالِبٍ يَا حُجَّةَ اللَّهِ عَلَى خَلْقِهِ، إِنَّا تَوَجَّهْنَا وَاسْتَشْفَعْنَا وَتَوَسَّلْنَا بِكَ إِلَى اللَّهِ، وَقَدَّمْنَاكَ بَيْنَ يَدَيْ حَاجَاتِنَا، يَا وَجِيهًا عِنْدَ اللَّهِ اشْفَعْ لَنَا عِنْدَ اللَّهِ.
يَا فَاطِمَةَ الزَّهْرَاءَ يَا بِنْتَ مُحَمَّدٍ يَا قُرَّةَ عَيْنِ الرَّسُولِ، إِنَّا تَوَجَّهْنَا وَاسْتَشْفَعْنَا وَتَوَسَّلْنَا بِكِ إِلَى اللَّهِ، وَقَدَّمْنَاكِ بَيْنَ يَدَيْ حَاجَاتِنَا، يَا وَجِيهَةً عِنْدَ اللَّهِ اشْفَعِي لَنَا عِنْدَ اللَّهِ.
يَا أَبَا مُحَمَّدٍ يَا حَسَنَ بْنَ عَلِيٍّ أَيُّهَا السِّبْطُ الْمُجْتَبَى، إِنَّا تَوَجَّهْنَا وَاسْتَشْفَعْنَا وَتَوَسَّلْنَا بِكَ إِلَى اللَّهِ، وَقَدَّمْنَاكَ بَيْنَ يَدَيْ حَاجَاتِنَا، يَا وَجِيهًا عِنْدَ اللَّهِ اشْفَعْ لَنَا عِنْدَ اللَّهِ.
يَا أَبَا عَبْدِ اللَّهِ يَا حُسَيْنَ بْنَ عَلِيٍّ أَيُّهَا السِّبْطُ الشَّهِيدُ، إِنَّا تَوَجَّهْنَا وَاسْتَشْفَعْنَا وَتَوَسَّلْنَا بِكَ إِلَى اللَّهِ، وَقَدَّمْنَاكَ بَيْنَ يَدَيْ حَاجَاتِنَا، يَا وَجِيهًا عِنْدَ اللَّهِ اشْفَعْ لَنَا عِنْدَ اللَّهِ.""",
            "en": """O Allah, I ask You and turn to You through Your Prophet, the Prophet of mercy, Muhammad (peace be upon him and his family). O Abu al-Qasim, O Messenger of Allah, O Imam of mercy, O intercessor of the ummah, O beloved of Allah — we turn to You, seek Your intercession, and take You as our means to Allah, and we place You before our needs. O one of high standing with Allah, intercede for us with Allah.
O Abu al-Hasan, O Commander of the Faithful, O Ali ibn Abi Talib, O proof of Allah over His creation — we turn to You, seek Your intercession, and take You as our means to Allah. O one of high standing with Allah, intercede for us with Allah.
O Fatimah al-Zahra, O daughter of Muhammad, O coolness of the Messenger’s eyes — we turn to you, seek your intercession, and take you as our means to Allah. O one of high standing with Allah, intercede for us with Allah.
O Abu Muhammad, O Hasan ibn Ali, O chosen grandson — we turn to You, seek Your intercession, and take You as our means to Allah. O one of high standing with Allah, intercede for us with Allah.
O Abu Abdillah, O Husayn ibn Ali, O martyred grandson — we turn to You, seek Your intercession, and take You as our means to Allah. O one of high standing with Allah, intercede for us with Allah.""",
            "source": "Mafatih al-Jinan · Dua Tawassul",
            "sect": "SHIA",
            "category": "SPECIAL",
        },
        {
            "title": "Ziyarat Ashura (core)",
            "arabic": """السَّلَامُ عَلَيْكَ يَا أَبَا عَبْدِ اللَّهِ، السَّلَامُ عَلَيْكَ يَا ابْنَ رَسُولِ اللَّهِ، السَّلَامُ عَلَيْكَ يَا ابْنَ أَمِيرِ الْمُؤْمِنِينَ وَابْنَ سَيِّدِ الْوَصِيِّينَ، السَّلَامُ عَلَيْكَ يَا ابْنَ فَاطِمَةَ الزَّهْرَاءِ سَيِّدَةِ نِسَاءِ الْعَالَمِينَ، السَّلَامُ عَلَيْكَ يَا ثَارَ اللَّهِ وَابْنَ ثَارِهِ وَالْوِتْرَ الْمَوْتُورَ، السَّلَامُ عَلَيْكَ وَعَلَى الْأَرْوَاحِ الَّتِي حَلَّتْ بِفِنَائِكَ، عَلَيْكُمْ مِنِّي جَمِيعًا سَلَامُ اللَّهِ أَبَدًا مَا بَقِيتُ وَبَقِيَ اللَّيْلُ وَالنَّهَارُ.
يَا أَبَا عَبْدِ اللَّهِ، لَقَدْ عَظُمَتِ الرَّزِيَّةُ وَجَلَّتْ وَعَظُمَتِ الْمُصِيبَةُ بِكَ عَلَيْنَا وَعَلَى جَمِيعِ أَهْلِ الْإِسْلَامِ، وَجَلَّتْ وَعَظُمَتْ مُصِيبَتُكَ فِي السَّمَاوَاتِ عَلَى جَمِيعِ أَهْلِ السَّمَاوَاتِ.
فَلَعَنَ اللَّهُ أُمَّةً أَسَّسَتْ أَسَاسَ الظُّلْمِ وَالْجَوْرِ عَلَيْكُمْ أَهْلَ الْبَيْتِ، وَلَعَنَ اللَّهُ أُمَّةً دَفَعَتْكُمْ عَنْ مَقَامِكُمْ وَأَزَالَتْكُمْ عَنْ مَرَاتِبِكُمُ الَّتِي رَتَّبَكُمُ اللَّهُ فِيهَا، وَلَعَنَ اللَّهُ أُمَّةً قَتَلَتْكُمْ، وَلَعَنَ اللَّهُ الْمُمَهِّدِينَ لَهُمْ بِالتَّمْكِينِ مِنْ قِتَالِكُمْ.
إِنِّي سِلْمٌ لِمَنْ سَالَمَكُمْ، وَحَرْبٌ لِمَنْ حَارَبَكُمْ إِلَى يَوْمِ الْقِيَامَةِ.""",
            "en": """Peace be upon you, O Abu Abdillah. Peace be upon you, O son of the Messenger of Allah. Peace be upon you, O son of the Commander of the Faithful and son of the master of the successors. Peace be upon you, O son of Fatimah al-Zahra, mistress of the women of the worlds. Peace be upon you, O blood of Allah and son of His blood, and the lone one who was left alone. Peace be upon you and upon the souls that gathered in your courtyard. Upon you all from me is the peace of Allah forever, as long as I remain and night and day remain.
O Abu Abdillah, the tragedy has been tremendous, and the calamity through you has been immense upon us and upon all the people of Islam, and your calamity was immense in the heavens upon all the people of the heavens.
So may Allah curse the people who laid the foundation of injustice and oppression against you, the People of the House; may Allah curse the people who pushed you from your station and removed you from the ranks in which Allah had placed you; may Allah curse the people who killed you; and may Allah curse those who paved the way for them by enabling them to fight you.
I am at peace with those who are at peace with you, and at war with those who wage war against you until the Day of Resurrection.""",
            "source": "Mafatih al-Jinan · Ziyarat Ashura",
            "sect": "SHIA",
            "category": "ZIYARAAT",
        },
        {
            "title": "Sahifa · Dua 1 Praise",
            "arabic": """الْحَمْدُ لِلَّهِ الْأَوَّلِ بِلَا أَوَّلٍ كَانَ قَبْلَهُ، وَالْآخِرِ بِلَا آخِرٍ يَكُونُ بَعْدَهُ، الَّذِي قَصُرَتْ عَنْ رُؤْيَتِهِ أَبْصَارُ النَّاظِرِينَ، وَعَجَزَتْ عَنْ نَعْتِهِ أَوْهَامُ الْوَاصِفِينَ.
ابْتَدَعَ بِقُدْرَتِهِ الْخَلْقَ ابْتِدَاعًا، وَاخْتَرَعَهُمْ عَلَى مَشِيَّتِهِ اخْتِرَاعًا، ثُمَّ سَلَكَ بِهِمْ طَرِيقَ إِرَادَتِهِ، وَبَعَثَهُمْ فِي سَبِيلِ مَحَبَّتِهِ، لَا يَمْلِكُونَ تَأْخِيرًا عَمَّا قَدَّمَهُمْ إِلَيْهِ، وَلَا يَسْتَطِيعُونَ تَقَدُّمًا إِلَى مَا أَخَّرَهُمْ عَنْهُ.
وَجَعَلَ لِكُلِّ رُوحٍ مِنْهُمْ قُوتًا مَعْلُومًا مَقْسُومًا مِنْ رِزْقِهِ، لَا يَنْقُصُ مَنْ زَادَهُ نَاقِصٌ، وَلَا يَزِيدُ مَنْ نَقَصَ مِنْهُمْ زَائِدٌ.
ثُمَّ ضَرَبَ لَهُ فِي الْحَيَاةِ أَجَلًا مَوْقُوتًا، وَنَصَبَ لَهُ أَمَدًا مَحْدُودًا، يَتَخَطَّى إِلَيْهِ بِأَيَّامِ عُمُرِهِ، وَيَرْهَقُهُ بِأَعْوَامِ دَهْرِهِ، حَتَّى إِذَا بَلَغَ أَقْصَى أَثَرِهِ، وَاسْتَوْعَبَ حِسَابَ عُمُرِهِ، قَبَضَهُ إِلَى مَا نَدَبَهُ إِلَيْهِ مِنْ مَوْفُورِ ثَوَابِهِ، أَوْ مَحْذُورِ عِقَابِهِ.""",
            "en": """All praise belongs to Allah, the First without a first before Him, and the Last without a last after Him; He whose vision the eyes of onlookers fall short of seeing, and whose description the imaginations of describers are unable to attain.
By His power He originated creation as an originality, and invented them according to His will as an invention. Then He made them travel the path of His will and sent them upon the way of His love. They possess no power to delay what He has advanced them toward, nor can they advance to what He has delayed them from.
He assigned for every soul among them a known, apportioned provision from His sustenance — none who decreases it decreases what He increased, and none who increases it increases what He decreased.
Then He struck for him in life a timed term, and set for him a limited span, which he steps toward through the days of his life and is overtaken by through the years of his age, until when he reaches the end of his trace and exhausts the account of his lifespan, He takes him to what He invited him toward of abundant reward or feared punishment.""",
            "source": "Sahifa Sajjadiya · Dua 1",
            "sect": "BOTH",
            "category": "SAHIFA",
        },
        {
            "title": "Sahifa · Dua 15 Needs",
            "arabic": """اللَّهُمَّ يَا غَايَةَ رَغْبَةِ الرَّاغِبِينَ، وَمُنْتَهَى طَلِبَةِ الطَّالِبِينَ، وَيَا مَنْ لَا غِنَى عَنْهُ، وَلَا بُدَّ مِنْهُ، وَيَا مَنْ لَا خَلَفَ لِفَائِتِهِ، وَلَا انْقِطَاعَ لِرِزْقِهِ.
أَسْأَلُكَ بِانْقِطَاعِ آمَالِي إِلَّا عَنْكَ، وَبِقِلَّةِ حِيلَتِي إِلَّا بِكَ، أَنْ تَجْعَلَ لِي مِنْ كُلِّ هَمٍّ فَرَجًا، وَمِنْ كُلِّ ضِيقٍ مَخْرَجًا، وَتَرْزُقَنِي مِنْ حَيْثُ أَحْتَسِبُ وَمِنْ حَيْثُ لَا أَحْتَسِبُ.
اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ، وَاقْضِ حَوَائِجِي كُلَّهَا، صَغِيرَهَا وَكَبِيرَهَا، وَيَسِّرْ لِي مَا تَعَسَّرَ، وَسَهِّلْ لِي مَا تَصَعَّبَ، وَاكْفِنِي مَا أَهَمَّنِي مِنْ أَمْرِ دُنْيَايَ وَآخِرَتِي.""",
            "en": """O Allah, O Goal of the desire of those who desire, and Ultimate Object of the request of those who ask; O One without Whom there is no richness and from Whom there is no escape; O One for Whose missed gift there is no substitute, and for Whose provision there is no cutoff.
I ask You by the cutting off of my hopes except from You, and by the scarcity of my means except through You, that You make for me a relief from every worry and a way out from every tightness, and that You provide for me from where I reckon and from where I do not reckon.
O Allah, bless Muhammad and the family of Muhammad, fulfill all my needs — small and great — ease for me what has become hard, make smooth for me what has become difficult, and suffice me in what concerns me of my worldly affair and my Hereafter.""",
            "source": "Sahifa Sajjadiya · Dua 15",
            "sect": "BOTH",
            "category": "SAHIFA",
        },
        {
            "title": "Munajat Sha'baniyyah (core)",
            "arabic": """اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ، وَاسْمَعْ دُعَائِي إِذَا دَعَوْتُكَ، وَاسْمَعْ نِدَائِي إِذَا نَادَيْتُكَ، وَأَقْبِلْ عَلَيَّ إِذَا نَاجَيْتُكَ، فَقَدْ هَرَبْتُ إِلَيْكَ، وَوَقَفْتُ بَيْنَ يَدَيْكَ مُسْتَكِينًا لَكَ، مُتَضَرِّعًا إِلَيْكَ.
أَرَجُو رَحْمَتَكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ، وَلَا يُيْئِسُنِي مِنْ رَوْحِكَ إِلَّا عَمَلِي، فَقَدْ قَرَعْتُ بَابَ فَضْلِكَ بِيَدِ الرَّجَاءِ، وَمَدَدْتُ إِلَيْكَ أَكُفَّ الضَّرَاعَةِ.
إِلَهِي إِنْ كُنْتَ لَا تَرْحَمُ إِلَّا أَهْلَ طَاعَتِكَ، فَإِلَى مَنْ يَفْزَعُ الْمُذْنِبُونَ؟ وَإِنْ كُنْتَ لَا تَقْبَلُ إِلَّا مِنْ أَهْلِ الِاسْتِقْصَاءِ، فَعَلَى مَنْ يَعْتَمِدُ الْمُفَرِّطُونَ؟
إِلَهِي أَسْأَلُكَ أَنْ تَمْلَأَ قَلْبِي حُبًّا لَكَ، وَخَشْيَةً مِنْكَ، وَتَصْدِيقًا بِكَ، وَيَقِينًا بِكَ، وَخَوْفًا مِنْكَ، وَشَوْقًا إِلَيْكَ.""",
            "en": """O Allah, bless Muhammad and the family of Muhammad, hear my prayer when I call You, hear my cry when I cry to You, and turn toward me when I whisper to You. For I have fled to You and stood before You, abased before You and pleading to You.
I hope for Your mercy which encompasses all things, and nothing makes me despair of Your relief except my own deeds. I have knocked on the door of Your favor with the hand of hope, and stretched toward You the palms of humility.
My God, if You only have mercy on the people of Your obedience, then to whom do the sinners flee? And if You only accept from those who fulfill perfectly, then upon whom do the negligent rely?
My God, I ask You to fill my heart with love for You, awe of You, belief in You, certainty in You, fear of You, and longing for You.""",
            "source": "Mafatih al-Jinan · Munajat Sha'baniyyah",
            "sect": "SHIA",
            "category": "MUNAJAAT",
        },
        {
            "title": "Aamal · Night Prayer intention",
            "arabic": """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
نَوَيْتُ أَنْ أُصَلِّيَ صَلَاةَ اللَّيْلِ قُرْبَةً إِلَى اللَّهِ تَعَالَى.
اللَّهُمَّ إِلَيْكَ تَوَجَّهْتُ، وَبِكَ آمَنْتُ، وَعَلَيْكَ تَوَكَّلْتُ، وَرَحْمَتَكَ رَجَوْتُ، وَمِنْ عَذَابِكَ اسْتَجَرْتُ.
رَبِّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ، وَارْزُقْنِي قِيَامَ اللَّيْلِ وَالْإِخْلَاصَ فِيهِ، وَاجْعَلْنِي مِنَ الَّذِينَ إِذَا ذُكِّرُوا بِآيَاتِ رَبِّهِمْ لَمْ يَخِرُّوا عَلَيْهَا صُمًّا وَعُمْيَانًا.""",
            "en": """In the name of Allah, the Most Gracious, the Most Merciful.
I intend to pray the night prayer seeking nearness to Allah the Exalted.
O Allah, toward You I turn, in You I believe, upon You I rely, Your mercy I hope for, and from Your punishment I seek refuge.
My Lord, help me to remember You, thank You, and worship You well; grant me the standing of the night and sincerity in it; and make me among those who, when reminded of the signs of their Lord, do not fall upon them deaf and blind.""",
            "source": "Mafatih · Night Aamal",
            "sect": "SHIA",
            "category": "AAMAL",
        },
        {
            "title": "Dua Faraj",
            "arabic": """اللَّهُمَّ كُنْ لِوَلِيِّكَ الْحُجَّةِ بْنِ الْحَسَنِ، صَلَوَاتُكَ عَلَيْهِ وَعَلَى آبَائِهِ، فِي هَذِهِ السَّاعَةِ وَفِي كُلِّ سَاعَةٍ، وَلِيًّا وَحَافِظًا وَقَائِدًا وَنَاصِرًا وَدَلِيلًا وَعَيْنًا، حَتَّى تُسْكِنَهُ أَرْضَكَ طَوْعًا، وَتُمَتِّعَهُ فِيهَا طَوِيلًا.
بِرَحْمَتِكَ يَا أَرْحَمَ الرَّاحِمِينَ.""",
            "en": """O Allah, be for Your guardian, the Proof, son of al-Hasan — Your blessings upon him and upon his fathers — in this hour and in every hour a protector, a guardian, a leader, a helper, a guide, and an eye, until You settle him in Your earth willingly and make him enjoy it for a long time.
By Your mercy, O Most Merciful of the merciful.""",
            "source": "Mafatih al-Jinan · Dua Faraj",
            "sect": "SHIA",
            "category": "SPECIAL",
        },
        {
            "title": "Sahifa · Dua 20 Noble Moral Traits",
            "arabic": """اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِهِ، وَبَلِّغْ بِإِيمَانِي أَكْمَلَ الْإِيمَانِ، وَاجْعَلْ يَقِينِي أَفْضَلَ الْيَقِينِ، وَانْتَهِ بِنِيَّتِي إِلَى أَحْسَنِ النِّيَّاتِ، وَبِعَمَلِي إِلَى أَحْسَنِ الْأَعْمَالِ.
اللَّهُمَّ وَفِّرْ بِلُطْفِكَ نِيَّتِي، وَصَحِّحْ بِمَا عِنْدَكَ يَقِينِي، وَاسْتَصْلِحْ بِقُدْرَتِكَ مَا فَسَدَ مِنِّي.
اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِهِ، وَاكْفِنِي مَا يَشْغَلُنِي الِاهْتِمَامُ بِهِ، وَاسْتَعْمِلْنِي بِمَا تَسْأَلُنِي غَدًا عَنْهُ، وَاسْتَفْرِغْ أَيَّامِي فِيمَا خَلَقْتَنِي لَهُ، وَأَغْنِنِي وَأَوْسِعْ عَلَيَّ فِي رِزْقِكَ، وَلَا تَفْتِنِّي بِالنَّظَرِ، وَأَعِزَّنِي وَلَا تَبْتَلِيَنِّي بِالْكِبْرِ، وَعَبِّدْنِي لَكَ وَلَا تُفْسِدْ عِبَادَتِي بِالْعُجْبِ.""",
            "en": """O Allah, bless Muhammad and his family, raise my faith to the most complete faith, make my certainty the best certainty, and take my intention to the best of intentions and my deeds to the best of deeds.
O Allah, through Your gentleness perfect my intention, correct through what is with You my certainty, and set right through Your power what is corrupt in me.
O Allah, bless Muhammad and his family, spare me what preoccupation with it distracts me, employ me in that for which You will ask me tomorrow, empty my days in that for which You created me, enrich me and expand for me in Your provision, do not try me with looking [at others’ wealth], honor me and do not afflict me with pride, and make me Your servant and do not corrupt my worship with self-admiration.""",
            "source": "Sahifa Sajjadiya · Dua 20",
            "sect": "BOTH",
            "category": "SAHIFA",
        },
        {
            "title": "Sahifa · Dua 21 Sorrow",
            "arabic": """يَا مَنْ تُحَلُّ بِهِ عُقَدُ الْمَكَارِهِ، وَيَا مَنْ يُفْثَأُ بِهِ حَدُّ الشَّدَائِدِ، وَيَا مَنْ يُلْتَمَسُ مِنْهُ الْمَخْرَجُ إِلَى رَوْحِ الْفَرَجِ.
ذَلَّتْ لِقُدْرَتِكَ الصِّعَابُ، وَتَسَبَّبَتْ بِلُطْفِكَ الْأَسْبَابُ، وَجَرَى بِقُدْرَتِكَ الْقَضَاءُ، وَمَضَتْ عَلَى إِرَادَتِكَ الْأَشْيَاءُ.
فَهِيَ بِمَشِيَّتِكَ دُونَ قَوْلِكَ مُؤْتَمِرَةٌ، وَبِإِرَادَتِكَ دُونَ نَهْيِكَ مُنْزَجِرَةٌ.
أَنْتَ الْمَدْعُوُّ لِلْمُهِمَّاتِ، وَأَنْتَ الْمَفْزَعُ فِي الْمُلِمَّاتِ، لَا يَنْدَفِعُ مِنْهَا إِلَّا مَا دَفَعْتَ، وَلَا يَنْكَشِفُ مِنْهَا إِلَّا مَا كَشَفْتَ.
وَقَدْ نَزَلَ بِي يَا رَبِّ مَا قَدْ تَكَأَّدَنِي ثِقَلُهُ، وَأَلَمَّ بِي مَا قَدْ بَهَظَنِي حَمْلُهُ.""",
            "en": """O He through Whom the knots of hardships are undone, O He through Whom the edge of adversities is blunted, O He from Whom the way out to the ease of relief is sought.
Difficulties are humbled before Your power, causes are set in motion by Your gentleness, destiny runs by Your power, and things proceed according to Your will.
So by Your wish — without Your spoken command — they obey, and by Your will — without Your prohibition — they refrain.
You are the One called for important matters, and You are the refuge in calamities. None of them is pushed away except what You push away, and none is lifted except what You lift.
There has descended upon me, my Lord, that whose weight has weighed me down, and there has come upon me that whose burden has overwhelmed me.""",
            "source": "Sahifa Sajjadiya · Dua 21",
            "sect": "BOTH",
            "category": "SAHIFA",
        },
        {
            "title": "Dua Nudbah (opening)",
            "arabic": """بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ، وَصَلَّى اللَّهُ عَلَى سَيِّدِنَا مُحَمَّدٍ نَبِيِّهِ وَآلِهِ وَسَلَّمَ تَسْلِيمًا.
اللَّهُمَّ لَكَ الْحَمْدُ عَلَى مَا جَرَى بِهِ قَضَاؤُكَ فِي أَوْلِيَائِكَ الَّذِينَ اسْتَخْلَصْتَهُمْ لِنَفْسِكَ وَدِينِكَ، إِذِ اخْتَرْتَ لَهُمْ جَزِيلَ مَا عِنْدَكَ مِنَ النَّعِيمِ الْمُقِيمِ الَّذِي لَا زَوَالَ لَهُ وَلَا اضْمِحْلَالَ، بَعْدَ أَنْ شَرَطْتَ عَلَيْهِمُ الزُّهْدَ فِي دَرَجَاتِ هَذِهِ الدُّنْيَا الدَّنِيَّةِ وَزُخْرُفِهَا وَزِبْرِجِهَا، فَشَرَطُوا لَكَ ذَلِكَ، وَعَلِمْتَ مِنْهُمُ الْوَفَاءَ، فَقَبِلْتَهُمْ وَقَرَّبْتَهُمْ، وَقَدَّمْتَ لَهُمُ الذِّكْرَ الْعَلِيَّ وَالثَّنَاءَ الْجَلِيَّ، وَأَهْبَطْتَ عَلَيْهِمْ مَلَائِكَتَكَ، وَكَرَّمْتَهُمْ بِوَحْيِكَ، وَرَفَدْتَهُمْ بِعِلْمِكَ، وَجَعَلْتَهُمُ الذَّرِيعَةَ إِلَيْكَ وَالْوَسِيلَةَ إِلَى رِضْوَانِكَ.""",
            "en": """In the name of Allah, the Most Gracious, the Most Merciful.
All praise belongs to Allah, Lord of the worlds, and may Allah bless our master Muhammad, His Prophet, and his family, and grant them peace.
O Allah, to You belongs praise for what Your decree has run concerning Your friends whom You chose for Yourself and Your religion, when You chose for them the abundant lasting bliss with You that has no end and no vanishing — after You conditioned upon them renunciation of the ranks of this lowly world and its glitter and ornament. So they accepted that condition for You, and You knew their loyalty, so You accepted them and drew them near, and You put forward for them lofty remembrance and clear praise, and You sent down upon them Your angels, and You honored them with Your revelation, and You supported them with Your knowledge, and You made them the means to You and the path to Your good pleasure.""",
            "source": "Mafatih al-Jinan · Dua Nudbah",
            "sect": "SHIA",
            "category": "SPECIAL",
        },
        {
            "title": "Ziyarat Warith",
            "arabic": """السَّلَامُ عَلَيْكَ يَا وَارِثَ آدَمَ صَفْوَةِ اللَّهِ، السَّلَامُ عَلَيْكَ يَا وَارِثَ نُوحٍ نَبِيِّ اللَّهِ، السَّلَامُ عَلَيْكَ يَا وَارِثَ إِبْرَاهِيمَ خَلِيلِ اللَّهِ، السَّلَامُ عَلَيْكَ يَا وَارِثَ مُوسَى كَلِيمِ اللَّهِ، السَّلَامُ عَلَيْكَ يَا وَارِثَ عِيسَى رُوحِ اللَّهِ، السَّلَامُ عَلَيْكَ يَا وَارِثَ مُحَمَّدٍ حَبِيبِ اللَّهِ، السَّلَامُ عَلَيْكَ يَا وَارِثَ أَمِيرِ الْمُؤْمِنِينَ عَلَيْهِ السَّلَامُ.
السَّلَامُ عَلَيْكَ يَا ابْنَ مُحَمَّدٍ الْمُصْطَفَى، السَّلَامُ عَلَيْكَ يَا ابْنَ عَلِيٍّ الْمُرْتَضَى، السَّلَامُ عَلَيْكَ يَا ابْنَ فَاطِمَةَ الزَّهْرَاءِ، السَّلَامُ عَلَيْكَ يَا ابْنَ خَدِيجَةَ الْكُبْرَى، السَّلَامُ عَلَيْكَ يَا ثَارَ اللَّهِ وَابْنَ ثَارِهِ وَالْوِتْرَ الْمَوْتُورَ.
أَشْهَدُ أَنَّكَ قَدْ أَقَمْتَ الصَّلَاةَ، وَآتَيْتَ الزَّكَاةَ، وَأَمَرْتَ بِالْمَعْرُوفِ، وَنَهَيْتَ عَنِ الْمُنْكَرِ، وَأَطَعْتَ اللَّهَ وَرَسُولَهُ حَتَّى أَتَاكَ الْيَقِينُ.""",
            "en": """Peace be upon you, O heir of Adam, the chosen of Allah. Peace be upon you, O heir of Noah, the prophet of Allah. Peace be upon you, O heir of Abraham, the friend of Allah. Peace be upon you, O heir of Moses, the one spoken to by Allah. Peace be upon you, O heir of Jesus, the spirit of Allah. Peace be upon you, O heir of Muhammad, the beloved of Allah. Peace be upon you, O heir of the Commander of the Faithful, peace be upon him.
Peace be upon you, O son of Muhammad the Chosen. Peace be upon you, O son of Ali the Approved. Peace be upon you, O son of Fatimah al-Zahra. Peace be upon you, O son of Khadijah the Greatest. Peace be upon you, O blood of Allah and son of His blood, and the lone one left alone.
I bear witness that you established the prayer, gave the zakat, enjoined good, forbade wrong, and obeyed Allah and His Messenger until certainty came to you.""",
            "source": "Mafatih al-Jinan · Ziyarat Warith",
            "sect": "SHIA",
            "category": "ZIYARAAT",
        },
        {
            "title": "Hisnul · After Prayer (extended)",
            "arabic": """أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ.
اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ.
لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ. اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ.
لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ. لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ، لَا إِلَهَ إِلَّا اللَّهُ، وَلَا نَعْبُدُ إِلَّا إِيَّاهُ، لَهُ النِّعْمَةُ وَلَهُ الْفَضْلُ وَلَهُ الثَّنَاءُ الْحَسَنُ، لَا إِلَهَ إِلَّا اللَّهُ مُخْلِصِينَ لَهُ الدِّينَ وَلَوْ كَرِهَ الْكَافِرُونَ.
سُبْحَانَ اللَّهِ، وَالْحَمْدُ لِلَّهِ، وَاللَّهُ أَكْبَرُ (ثَلَاثًا وَثَلَاثِينَ)، ثُمَّ: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.""",
            "en": """I seek Allah’s forgiveness, I seek Allah’s forgiveness, I seek Allah’s forgiveness.
O Allah, You are Peace and from You is peace; blessed are You, O Owner of majesty and honor.
There is no god but Allah alone, with no partner. His is the dominion and His is the praise, and He is over all things competent. O Allah, none can withhold what You give, and none can give what You withhold, and the fortune of the fortunate does not avail against You.
There is no god but Allah alone, with no partner. His is the dominion and His is the praise, and He is over all things competent. There is no power and no strength except with Allah. There is no god but Allah, and we worship none but Him. His is the blessing, His is the favor, and His is the fine praise. There is no god but Allah, making the religion purely for Him even if the disbelievers dislike it.
Glory be to Allah, and praise be to Allah, and Allah is the Greatest (thirty-three times), then: There is no god but Allah alone, with no partner. His is the dominion and His is the praise, and He is over all things competent.""",
            "source": "Hisnul Muslim · After Salah",
            "sect": "SUNNI",
            "category": "NAMAZ",
        },
    ]


def main():
    items = load_hisnul()
    items.extend(curated_shia_extra())
    print("items", len(items), flush=True)

    done = {}
    if os.path.exists(PROGRESS):
        with open(PROGRESS, encoding="utf-8") as f:
            done = json.load(f)

    ur_t = GoogleTranslator(source="en", target="ur")
    fa_t = GoogleTranslator(source="en", target="fa")

    for i, it in enumerate(items):
        key = f"{it['sect']}|{it['category']}|{it['title']}"
        if key in done and done[key].get("ur") and done[key].get("fa"):
            it["ur"], it["fa"] = done[key]["ur"], done[key]["fa"]
            continue
        try:
            it["ur"] = tr(ur_t, it["en"])
            it["fa"] = tr(fa_t, it["en"])
        except Exception as e:
            print("fail", it["title"], e, flush=True)
            it["ur"] = it["en"]
            it["fa"] = it["en"]
        done[key] = {"ur": it["ur"], "fa": it["fa"]}
        if (i + 1) % 10 == 0:
            with open(PROGRESS, "w", encoding="utf-8") as f:
                json.dump(done, f, ensure_ascii=False)
            print(f"tr {i+1}/{len(items)}", flush=True)

    with open(PROGRESS, "w", encoding="utf-8") as f:
        json.dump(done, f, ensure_ascii=False)

    if os.path.exists(OUT_DB):
        os.remove(OUT_DB)
    con = sqlite3.connect(OUT_DB)
    con.execute(
        """CREATE TABLE content (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            arabic_text TEXT NOT NULL,
            translation TEXT NOT NULL,
            translation_ur TEXT,
            translation_ar TEXT,
            translation_fa TEXT,
            source TEXT,
            sect_tag TEXT NOT NULL,
            category TEXT NOT NULL
        )"""
    )
    for it in items:
        con.execute(
            """INSERT INTO content
            (title, arabic_text, translation, translation_ur, translation_ar,
             translation_fa, source, sect_tag, category)
            VALUES (?,?,?,?,?,?,?,?,?)""",
            (
                it["title"],
                it["arabic"],
                it["en"],
                it.get("ur", it["en"]),
                None,  # AR UI uses Arabic body + EN meaning
                it.get("fa", it["en"]),
                it["source"],
                it["sect"],
                it["category"],
            ),
        )
    con.commit()
    n = con.execute("SELECT COUNT(*) FROM content").fetchone()[0]
    avg = con.execute("SELECT AVG(LENGTH(arabic_text)) FROM content").fetchone()[0]
    print("wrote", OUT_DB, "rows", n, "avg_ar_len", int(avg or 0), flush=True)
    con.close()


if __name__ == "__main__":
    main()
