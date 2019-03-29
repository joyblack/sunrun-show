package com.sunrun.movieshow.service;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.collection.AhoCorasick.AhoCorasickDoubleArrayTrie;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.model.crf.CRFLexicalAnalyzer;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.suggest.Suggester;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.hankcs.hanlp.tokenizer.SpeedTokenizer;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.sunrun.movieshow.bean.NLPData;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * HanNLP使用教程
 */
@Service
public class HanNLPService {

    // 1.标准分词
    public String standard(String sentence){
        List<Term> termList = StandardTokenizer.segment(sentence);
        // 等价于HanLP.segment("你好，欢迎使用HanLP汉语处理包！")
        return termList.toString();
    }

    // 2.NLP分词
    public String nlp(String content){
        return NLPTokenizer.segment(content).toString();
        // 注意观察下面两个“希望”的词性、两个“晚霞”的词性
        //System.out.println(NLPTokenizer.analyze("我的希望是希望张晚霞的背影被晚霞映红").translateLabels());
        //System.out.println(NLPTokenizer.analyze("支援臺灣正體香港繁體：微软公司於1975年由比爾·蓋茲和保羅·艾倫創立。"));
    }

    // 3.索引分词
    public String index(String content){
        return IndexTokenizer.segment(content).toString();
    }

    // 4.N-最短路径分词
    public void nShort(String content){
        Segment nShortSegment = new NShortSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
        Segment shortestSegment = new DijkstraSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
        String[] testCase = new String[]{
                "今天，刘志军案的关键人物,山西女商人丁书苗在市二中院出庭受审。",
                "刘喜杰石国祥会见吴亚琴先进事迹报告团成员",
        };
        for (String sentence : testCase)
        {
            System.out.println("N-最短分词：" + nShortSegment.seg(sentence) + "\n最短路分词：" + shortestSegment.seg(sentence));
        }
    }

    // 5.CRF分词：CRF对新词有很好的识别能力，但是开销较大。
    public void crf() throws IOException {
        CRFLexicalAnalyzer analyzer = new CRFLexicalAnalyzer();
        String[] tests = new String[]{
                "商品和服务",
                "上海华安工业（集团）公司董事长谭旭光和秘书胡花蕊来到美国纽约现代艺术博物馆参观",
                "微软公司於1975年由比爾·蓋茲和保羅·艾倫創立，18年啟動以智慧雲端、前端為導向的大改組。" // 支持繁体中文
        };
        for (String sentence : tests)
        {
            System.out.println(analyzer.analyze(sentence));
        }
    }

    // 6.演示极速分词，基于AhoCorasickDoubleArrayTrie实现的词典分词，适用于“高吞吐量”“精度一般”的场合
    public void speed(){
            String text = "江西鄱阳湖干枯，中国最大淡水湖变成大草原";
            System.out.println(SpeedTokenizer.segment(text));
            long start = System.currentTimeMillis();
            int pressure = 1000000;
            for (int i = 0; i < pressure; ++i)
            {
                SpeedTokenizer.segment(text);
            }
            double costTime = (System.currentTimeMillis() - start) / (double)1000;
            System.out.printf("分词速度：%.2f字每秒", text.length() * pressure / costTime);
    }

    // 演示用户词典的动态增删
    public void customDictionary() {
        // 动态增加
        CustomDictionary.add("攻城狮");
        // 强行插入
        CustomDictionary.insert("白富美", "nz 1024");
        // 删除词语（注释掉试试）
        // CustomDictionary.remove("攻城狮");
        System.out.println(CustomDictionary.add("单身狗", "nz 1024 n 1"));
        System.out.println(CustomDictionary.get("单身狗"));

        String text = "攻城狮逆袭单身狗，迎娶白富美，走上人生巅峰";  // 怎么可能噗哈哈！

        // AhoCorasickDoubleArrayTrie自动机扫描文本中出现的自定义词语
        final char[] charArray = text.toCharArray();
        CustomDictionary.parseText(charArray, new AhoCorasickDoubleArrayTrie.IHit<CoreDictionary.Attribute>() {
            @Override
            public void hit(int begin, int end, CoreDictionary.Attribute value) {
                System.out.printf("[%d:%d]=%s %s\n", begin, end, new String(charArray, begin, end - begin), value);
            }
        });
        // 自定义词典在所有分词器中都有效
        System.out.println(HanLP.segment(text));
    }

    // 中国人名识别
    public String chineseName(String content){
        Segment segment = HanLP.newSegment().enableNameRecognize(true);
        return segment.seg(content).toString();
    }

    // 音译人名识别
    public String yinName(String content){
        Segment segment = HanLP.newSegment().enableTranslatedNameRecognize(true);
        return segment.seg(content).toString();
    }

    // 日本人名识别(默认关闭)
    public String japaneseName(String content){
        Segment segment = HanLP.newSegment().enableJapaneseNameRecognize(true);
        return segment.seg(content).toString();
    }

    // 所有名字识别
    // 目前分词器基本上都默认开启了中国人名、音译人名识别、
    // 分词器默认关闭了日本人名识别，用户需要手动开启；这是因为日本人名的出现频率较低，但是又消耗性能。
    public String allName(String content){
        Segment segment = HanLP.newSegment().enableJapaneseNameRecognize(true);
        return segment.seg(content).toString();
    }



    // 地名识别
    public String placeName(String content){
        Segment segment = HanLP.newSegment().enablePlaceRecognize(true);
        return segment.seg(content).toString();
    }

    // 机构名识别
    public String organization(String content){
        Segment segment = HanLP.newSegment().enableOrganizationRecognize(true);
        return segment.seg(content).toString();
    }


    // 关键字提取 size为提取的关键字个数
    public String keyword(String content,Integer size){
        List<String> keywordList = HanLP.extractKeyword(content, size);
        return keywordList.toString();
    }

    // 自动摘要
    public String summary(String content,Integer size){
        List<String> sentenceList = HanLP.extractSummary(content, size);
        return sentenceList.toString();
    }

    // 短语提取
    public String phrase(String content,Integer size){
        List<String> phraseList = HanLP.extractPhrase(content, size);
        return phraseList.toString();
    }

    // 拼音转换
    public String pin(String text){
        StringBuilder builder = new StringBuilder();
        List<Pinyin> pinyinList = HanLP.convertToPinyinList(text);
        builder.append("原文: ");
        for (char c : text.toCharArray())
        {
            builder.append(String.format("%c,", c));
        }
        builder.append("<br/><br/>");

        builder.append("拼音（数字音调）:");
        for (Pinyin pinyin : pinyinList)
        {
            builder.append(String.format("%s,", pinyin));
        }
        builder.append("<br/><br/>");

        builder.append("拼音（符号音调）:");
        for (Pinyin pinyin : pinyinList)
        {
            builder.append(String.format("%s,", pinyin.getPinyinWithToneMark()));
        }
        System.out.println();

        builder.append("拼音（无音调）:");
        for (Pinyin pinyin : pinyinList)
        {
            builder.append(String.format("%s,", pinyin.getPinyinWithoutTone()));
        }
        builder.append("<br/><br/>");

        builder.append("声调:");
        for (Pinyin pinyin : pinyinList)
        {
            builder.append(String.format("%s,", pinyin.getTone()));
        }
        builder.append("<br/><br/>");

        builder.append("声母:");
        for (Pinyin pinyin : pinyinList)
        {
            builder.append(String.format("%s,", pinyin.getShengmu()));
        }
        builder.append("<br/><br/>");

        builder.append("韵母:");
        for (Pinyin pinyin : pinyinList)
        {
            builder.append(String.format("%s,", pinyin.getYunmu()));
        }
        builder.append("<br/><br/>");

        builder.append("输入法头:");
        for (Pinyin pinyin : pinyinList)
        {
            builder.append(String.format("%s,", pinyin.getHead()));
        }
        return builder.toString();
    }

    // 简繁转换
    public String t2s(NLPData data){
        if(data.getType().equals("t")){
            return HanLP.convertToTraditionalChinese(data.getContent());
        }else{
            return HanLP.convertToSimplifiedChinese(data.getContent());
        }
    }

    // 文本推荐
    public void suggest(){
        Suggester suggester = new Suggester();
        String[] titleArray =
                (
                        "威廉王子发表演说 呼吁保护野生动物\n" +
                                "《时代》年度人物最终入围名单出炉 普京马云入选\n" +
                                "“黑格比”横扫菲：菲吸取“海燕”经验及早疏散\n" +
                                "日本保密法将正式生效 日媒指其损害国民知情权\n" +
                                "英报告说空气污染带来“公共健康危机”"
                ).split("\\n");
        for (String title : titleArray)
        {
            suggester.addSentence(title);
        }

        System.out.println(suggester.suggest("发言", 1));       // 语义
        System.out.println(suggester.suggest("危机公共", 1));   // 字符
        System.out.println(suggester.suggest("mayun", 1));      // 拼音
    }

//    // 语义距离
//    public void word2c(){
//        WordVectorModel wordVectorModel = trainOrLoadModel();
//        printNearest("中国", wordVectorModel);
//        printNearest("美丽", wordVectorModel);
//        printNearest("购买", wordVectorModel);
//
//        // 文档向量
//        DocVectorModel docVectorModel = new DocVectorModel(wordVectorModel);
//        String[] documents = new String[]{
//                "山东苹果丰收",
//                "农民在江苏种水稻",
//                "奥运会女排夺冠",
//                "世界锦标赛胜出",
//                "中国足球失败",
//        };
//
//        System.out.println(docVectorModel.similarity(documents[0], documents[1]));
//        System.out.println(docVectorModel.similarity(documents[0], documents[4]));
//
//        for (int i = 0; i < documents.length; i++)
//        {
//            docVectorModel.addDocument(i, documents[i]);
//        }
//
//        printNearestDocument("体育", documents, docVectorModel);
//        printNearestDocument("农业", documents, docVectorModel);
//        printNearestDocument("我要看比赛", documents, docVectorModel);
//        printNearestDocument("要不做饭吧", documents, docVectorModel);
//    }

    // 依存句法分析
    public String dependencyParser(NLPData data){
        StringBuilder builder = new StringBuilder();
        CoNLLSentence sentence = HanLP.parseDependency(data.getContent());
        System.out.println(sentence);
        // 可以方便地遍历它
        for (CoNLLWord word : sentence)
        {
            builder.append(String.format("%s --(%s)--> %s<br/>", word.LEMMA, word.DEPREL, word.HEAD.LEMMA));
        }

        builder.append("###################<br/>");
        // 也可以直接拿到数组，任意顺序或逆序遍历
        CoNLLWord[] wordArray = sentence.getWordArray();
        for (int i = wordArray.length - 1; i >= 0; i--)
        {
            CoNLLWord word = wordArray[i];
            builder.append(String.format("%s --(%s)--> %s<br/>", word.LEMMA, word.DEPREL, word.HEAD.LEMMA));
        }

        return builder.toString();
    }



    public String seg(NLPData data) {
        String result = "";
        switch (data.getType()) {
            case "standard": result = standard(data.getContent());break;
            case "nlp"      : result = nlp(data.getContent());break;
            case "index"    : result = index(data.getContent());break;
        }
        return result;
    }

    public String name(NLPData data) {
        String result = "";
        switch (data.getType()) {
            case "chinese": result = chineseName(data.getContent());break;
            case "japan"      : result = japaneseName(data.getContent());break;
            case "trans"    : result = yinName(data.getContent());break;
            default: result = allName(data.getContent());break;
        }
        return result;
    }

    public String place(NLPData data) {
        return placeName(data.getContent());
    }

    // 文本推荐(句子级别，从一系列句子中挑出与输入句子最相似的那一个)
    public String recommend(NLPData data) {
        Suggester suggester = new Suggester();
        String[] titleArray =
                (
                        "威廉王子发表演说 呼吁保护野生动物\n" +
                                "《时代》年度人物最终入围名单出炉 普京马云入选\n" +
                                "“黑格比”横扫菲：菲吸取“海燕”经验及早疏散\n" +
                                "日本保密法将正式生效 日媒指其损害国民知情权\n" +
                                "英报告说空气污染带来“公共健康危机”"
                ).split("\\n");
        // 添加一系列的句子
        for (String title : titleArray)
        {
            suggester.addSentence(title);
        }
        return suggester.suggest(data.getContent(),data.getSize()).toString();
    }
}
