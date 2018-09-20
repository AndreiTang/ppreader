package org.andrei.ppreader.service;

import android.app.Application;

import org.andrei.ppreader.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by andrei on 2018/9/11.
 */

public class CrawlNovel implements ICrawlNovel {


    @Override
    public Observable<PPNovel> search(String name) {
        return Observable.create(new ObservableOnSubscribe<PPNovel>() {

            @Override
            public void subscribe(ObservableEmitter<PPNovel> emitter) throws Exception {

//                Thread.sleep(5000);
//                String msg = Integer.toString(R.string.err_network);
//                emitter.onError(new Throwable(msg));

                PPNovel novel = new PPNovel();
                novel.author = "爱潜水的乌贼";
                novel.chapterUrl = "https://www.88dus.com/xiaoshuo/38/38089/";
                novel.desc = "知识就等于力量。所谓神，不过是强大一点的奥术师。带着一大堆知识的夏风穿越而来了。";
                novel.engineIndex = 0;
                novel.imgUrl = "https://fm.88dus.com/38/38089/38089s.jpg";
                novel.name = "奥术神座";
                emitter.onNext(novel);
                Thread.sleep(1000);

                novel = new PPNovel();
                novel.author = "爱潜水的乌贼";
                novel.chapterUrl = "https://www.88dus.com/xiaoshuo/81/81340/";
                novel.desc = " 第一，不要笑书名。第二，不要笑封面。第三，不要笑简介。如果大家上面三句话会心笑了，说明本书风格应该挺适合你们的。在这里，武道不再是虚无缥缈的传说，而是切切实实的传承，经过与科技的对抗后，彻底融入了社会，有了各种各样的武道比赛，文无第一，武无第二！楼成得到武道一大流派断绝的传承后，向着最初的梦想，向着心里的荣耀......";
                novel.engineIndex = 0;
                novel.imgUrl = "https://fm.88dus.com/81/81340/81340s.jpg";
                novel.name = "武道宗师";
                emitter.onNext(novel);
                Thread.sleep(1000);

                novel = new PPNovel();
                novel.author = "爱潜水的乌贼";
                novel.chapterUrl = "https://www.88dus.com/xiaoshuo/102/102560/";
                novel.desc = "蒸汽与机械的浪潮中，谁能触及非凡？历史和黑暗的迷雾里，又是谁在耳语？我从诡秘中醒来，睁眼看见这个世界：枪械，大炮，巨舰，飞空艇，差分机；魔药，占卜，诅咒，倒吊人，封印物……光明依旧照耀，神秘从未远离，这是一段“愚者”的传说。......";
                novel.engineIndex = 0;
                novel.imgUrl = "https://fm.88dus.com/102/102560/102560s.jpg";
                novel.name = "诡秘之主";
                emitter.onNext(novel);
                Thread.sleep(1000);

                novel = new PPNovel();
                novel.author = "爱潜水的乌贼";
                novel.chapterUrl = "https://www.88dus.com/xiaoshuo/26/26085/";
                novel.desc = "修真，去假存真，照见本性。能达到这点的，则被称为“真人”，他们成就元神，超脱生死。灭运图录，灭运道种？一个偶得上古仙法的穿越客在这诸天万界、亿兆大千世界的修炼故事。";
                novel.engineIndex = 0;
                novel.imgUrl = "https://fm.88dus.com/26/26085/26085s.jpg";
                novel.name = "灭运图录";
                emitter.onNext(novel);
                Thread.sleep(1000);

                novel = new PPNovel();
                novel.author = "幸运的苏拉";
                novel.chapterUrl = "https://www.88dus.com/xiaoshuo/39/39670/";
                novel.desc = "条条大路通罗马，但来的却并不都是罗马想要的。\n" +
                        " 斗兽场是小的罗马，罗马是大的斗兽场。\n" +
                        " 他，李必达，不过是个精通语言学的普通大学生，却有幸穿越到了彼时，罗马建城678年，耶稣诞辰前66年，但他没有成为显贵，因为不可能；没有成为所谓发明家，因为不可能；他最初的身份，就是个军队奴隶，也是最合情合理的身份，自此开始了波澜壮阔的冒险生涯。\n" +
                        " 凯撒、庞培、西塞罗、克拉苏、安东尼、屋大维、米特拉达梯不再是呆滞的蜡像。\n" +
                        " 百夫长、商贩、拍卖人、修辞学家、女奴、祭司、娼妓不再是死板的文字。\n" +
                        " 本都王女、贵族遗孀、亚马逊女王、埃及艳后不再是桃色的梦想。\n" +
                        " 奴隶、自由奴、有产公民、骑士、度支官、军事护民官、骑兵长官、元老......狄克推多，这条铺满骸骨、头颅的道路走下来，谁能成为笑到最后的，独一无二的奥古斯都？";
                novel.engineIndex = 0;
                novel.imgUrl = "https://fm.88dus.com/39/39670/39670s.jpg";
                novel.name = "奥古斯都之路";
                emitter.onNext(novel);
                Thread.sleep(1000);

                Thread.sleep(10000);
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<CrawlChapterResult> fetchChapters(final PPNovel novel) {
        return Observable.create(new ObservableOnSubscribe<CrawlChapterResult>() {
            @Override
            public void subscribe(ObservableEmitter<CrawlChapterResult> e) throws Exception {
                CrawlChapterResult ret = new CrawlChapterResult();
                ret.chapterUrl = novel.chapterUrl;
                e.onNext(ret);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<CrawlTextResult> fetchNovelText(final String novelId, final String chapterUrl) {
        return Observable.create(new ObservableOnSubscribe<CrawlTextResult>() {
            @Override
            public void subscribe(ObservableEmitter<CrawlTextResult> e) throws Exception {
                CrawlTextResult ret = new CrawlTextResult();
                ret.chapterUrl = chapterUrl;
                ret.novelUrl = novelId;
                ret.text = " 傍晚时分的阳光洞穿一朵朵赤红色的云彩，照在静穆的阿得让教堂广场上，主教手中举着的小太阳已经失去明亮的光辉，被他挂在胸前，转身往教堂之内走去。\n" +
                        "\n" +
                        "    广场中央，颇为美丽的黑袍女巫已经被烧成了灰烬，但疯狂的笑声和诅咒仿佛还在回荡，让不少人忍不住哆嗦了一下，看了看四周，然后跟着主教和牧师们走进教堂，在主的关注下忏悔着自己的罪行，诚心地做着祈祷。\n" +
                        "\n" +
                        "    夺目的白光似乎还在眼前，其蕴含着的神圣而磅礴的力量仿佛还能依稀感觉，路西恩在这冲击之下，已经接受了自己的身份，将过往的一切深深埋葬在心中，不敢露出丝毫异样。\n" +
                        "\n" +
                        "    “神术的力量好强大，不知我有没有机会学到？”\n" +
                        "\n" +
                        "    路西恩身心震撼地想着，没有普通人该有的敬畏，突然，一股巨大的力量拍在了路西恩的左肩，拍得他不由自主往左边倾倒，险些就站立不稳。\n" +
                        "\n" +
                        "    “噢，我可怜的小伊文斯，你总算没事了，这都是主的庇佑，我还以为你会跟你父亲那样，病倒了就再也没能站起来，感谢主，让这么棒的好小伙能继续活着。”\n" +
                        "\n" +
                        "    路西恩被这么一拍，已经从震撼失神中清醒过来，看到一位足有两个自己宽的褐发大婶站在旁边，一边抹着眼泪，一边用可以媲美熊掌攻击的右手频频拍着自己的肩膀。\n" +
                        "\n" +
                        "    稍稍闪开，免得自己被拍得吐血，路西恩张了张嘴，却发现一句话也说不出来，因为：“这位大婶该怎么称呼？似乎我的全名应该是路西恩?伊文斯？”\n" +
                        "\n" +
                        "    路西恩这一躲，让那位大婶更加悲伤：“可怜的小伊文斯，你一定是病得迷糊了，你看这小脸蛋，瘦得都可以看见骨头了……”\n" +
                        "\n" +
                        "    念叨中，路西恩尴尬无比，自己穿越而来，除了能听懂、能说这里的语言，什么记忆都没有继承，要是应对错误的话，很可能被人误会为魔鬼附身，当然，从某种意义来讲，现在的路西恩确实是被魔鬼侵占了灵魂。\n" +
                        "\n" +
                        "    好在这时，旁边站着的一位中年男子拍了拍胖大婶，安抚道：“艾丽萨，小伊文斯刚刚才好，精神一定很虚弱，你不要吵他。艾文，扶着你妈妈，我们一起回家。”\n" +
                        "\n" +
                        "    这位中年男子身材很瘦，背微微弓着，有一头略显花白的金色短发，被时间刻上了沧桑的脸上依稀可以看到年轻时的英俊。\n" +
                        "\n" +
                        "    但在路西恩眼中，这位中年大叔此时就像一位带着洁白光环的天使，将自己从尴尬、紧张中脱离的天使。\n" +
                        "\n" +
                        "    “艾丽萨大婶，我已经全好了，只是还有点头晕。”路西恩筹措着词语，免得露出马脚。\n" +
                        "\n" +
                        "    那位拉着路西恩来看烧女巫的小男孩艾文半扶着他**妈，扮着鬼脸：“路西恩大哥可不是那种生一次病就死掉的懦夫，只有你还当他是需要照顾的小不点。”\n" +
                        "\n" +
                        "    艾丽萨大婶抹着眼泪：“小伊文斯，看到你好起来，我就放心了，都怪那该死的、邪恶的、已经下地狱的女巫。”\n" +
                        "\n" +
                        "    被艾文扶着，艾丽萨大婶一边慢慢向前走着，一边唠叨：“她刚刚搬到你旁边屋子的时候，是多么的文静美丽，多么的温文尔雅，我还想着小约翰要是能够娶到这样的姑娘，那就是主的恩赐，可是，可她竟然是女巫，而且还跑去墓园偷死者的遗骸来施展邪恶的魔法，幸好主的荣光照耀一切，当时有裁判所的守夜人就在墓园，将她直接抓住，要不然被她准备好邪恶的魔法，我们阿得让区不知要死多少人……”\n" +
                        "\n" +
                        "    与中年大叔一起走在后面，从艾丽萨大婶的唠叨里，路西恩大概知道了事情的经过，那位女巫是在去墓园偷尸体的时候被教会的守夜人抓住，而自己作为她的邻居，也被教会找去审问，中间教会可能用了一些神术上的手段，在排除了那位真正路西恩嫌疑的同时，也让他受到了精神或是肉体上的伤害，于是得了一场大病而死去，所以才能让自己附身。\n" +
                        "\n" +
                        "    见路西恩没有说话，中年大叔拍了拍路西恩的肩膀，悄声安慰：“艾丽萨就是这样唠叨，你当做没听见就好了。”\n" +
                        "\n" +
                        "    路西恩不知该说什么，只能点了点头。\n" +
                        "\n" +
                        "    中年大叔看着艾丽萨大婶的背影，悄悄叹了口气：“哎，当年的艾丽萨可是一位纯洁而热情的美丽姑娘，但自从生了小约翰之后，就像是被魔鬼诅咒了一样，短短一年就变成了现在的样子。”\n" +
                        "\n" +
                        "    他一副似乎充满人生感慨的模样，目光颇为深邃，然后顿了顿：“我再也打不过她了。”\n" +
                        "\n" +
                        "    刚穿越过来，又亲眼看见和体会到了神术的威力，路西恩受到极大冲击，正处在心神不宁中，因此只是勉强笑了笑，没有直接回答，而且他还不知道这位大叔该怎么称呼。\n" +
                        "\n" +
                        "    也许是大叔回忆得太过入神，没有注意到声音的控制，艾丽萨哼了一声：“乔尔，你这位充满理想和激情的吟游诗人，千辛万苦到阿尔托来追寻音乐梦想的青年，还不是也变成了整天醉醺醺的酒鬼。”\n" +
                        "\n" +
                        "    乔尔讪讪笑了笑：“阿尔托是圣咏之城，每天不知多少追寻音乐梦想的年轻人到来，可其中能够成功的又有多少？艾丽萨，再说，自从小约翰开始锻炼以来，我不就戒酒了吗？”\n" +
                        "\n" +
                        "    艾丽萨大婶回头瞪了他一眼：“幸好有主的庇佑，你还知道我们的希望都在约翰和艾文身上。要不是小约翰天天锻炼，怎么可能被维恩爵士挑中，去他的庄园进行正规的骑士训练，要是他能激发血脉内的‘神恩’，成为真正的骑士，那就能被大公封为勋爵，成为尊敬的贵族。”\n" +
                        "\n" +
                        "    严厉的目光让乔尔缩了缩肩膀，但艾丽萨很快注意到了一边路西恩的恍惚：“哦，抱歉，小伊文斯，婶婶不是故意提起这件事情的，你也很有天赋，只是，只是没有从小开始锻炼……”\n" +
                        "\n" +
                        "    发现自己越说越是触痛路西恩内心的伤痕，艾丽萨忙闭上了嘴，用眼神示意乔尔说话。\n" +
                        "\n" +
                        "    乔尔哈哈大笑，再次拍了拍路西恩的肩膀：“我们的小伊文斯怎么可能这么脆弱，他可是要继承他乔尔叔叔音乐家梦想的男人。”\n" +
                        "\n" +
                        "    心神动荡不安的路西恩只好咧嘴笑了笑：“是啊，我的梦想是成为音乐家。”\n" +
                        "\n" +
                        "    见路西恩没什么异状，艾丽萨又继续唠叨着种种琐事，倒是让路西恩对自己所处的这个城市有了进一步的了解。\n" +
                        "\n" +
                        "    这是一个叫做阿尔托的大城，在黑暗山脉附近，有圣咏之城的称号，繁荣，充满机会。\n" +
                        "\n" +
                        "    目前自己居住的地方，是阿尔托的贫民聚集地阿得让区，并且自己似乎因为几天的大病，丢掉了在市场区帮忙搬运货物的固定工作。\n" +
                        "\n" +
                        "    很快，四人走到了路西恩所在的小屋前。\n" +
                        "\n" +
                        "    艾丽萨大婶本来想邀请路西恩去她家吃晚饭，但急需要安静的路西恩委婉地拒绝了她。\n" +
                        "\n" +
                        "    分开时，艾文悄悄而好奇地问着路西恩：“路西恩大哥，你的梦想什么时候变成音乐家了？”\n" +
                        "\n" +
                        "    “刚才。”路西恩麻木地动了动嘴巴。\n" +
                        "\n" +
                        "    艾文长长地喔了一声。\n" +
                        "\n" +
                        "    进了屋，将门反锁，路西恩神不守舍地坐下，手肘支在木桌上，而脑袋则深深地埋在手肘里。\n" +
                        "\n" +
                        "    “我竟然穿越了！”\n" +
                        "\n" +
                        "    “而且还是这种有着非人力量的世界。”\n" +
                        "\n" +
                        "    “一不小心就会被绑在火刑架上活活烧死。”\n" +
                        "\n" +
                        "    刚才目睹女巫凄惨下场而产生的情绪，在没有人的时候，终于爆发了出来，路西恩是又惊又吓又怕。\n" +
                        "\n" +
                        "    当然，作为一名没有经历过多少大事的家伙，路西恩虽然有些内向，遇到事情容易慌张，但在那巨大压力面前，竟然还是保持住了冷静，直到回来才压制不住。\n" +
                        "\n" +
                        "    不得不说，环境和遭遇最为磨砺人。\n" +
                        "\n" +
                        "    随着时间推移，黑暗渐渐降临，路西恩也适应了恐惧，镇定下来，既然都已经穿越，再惶恐、再担心、再害怕，都于事无补，只能小心翼翼地见一步走一步，这次再死掉，可不一定能再有穿越的好事情了。\n" +
                        "\n" +
                        "    情绪恢复，压住对父母、朋友的思念和担心，刚想规划一下人生，路西恩立刻就感觉到汹涌袭来的饥饿感，胃里面就像有一把火在燃烧，口水不停地分泌。\n" +
                        "\n" +
                        "    吞了吞口水，填饱肚子是当务之急，路西恩中断了思考，往屋子里唯一能存放东西的板条箱走去。\n" +
                        "\n" +
                        "    破旧的箱子里，除了一些衣物，路西恩看到了两条黑色的面包状食物，以及七个闪烁着金属光芒的黄铜钱币。\n" +
                        "\n" +
                        "    胃袋已经代替了路西恩的大脑，他没有多余的精力去想别的事情，拿起一条黑面包就啃了下去。\n" +
                        "\n" +
                        "    喀嚓一声，路西恩连忙用手捂住嘴巴，牙齿差点给崩掉了，这究竟是面包还是木棍？\n" +
                        "\n" +
                        "    闻了闻，路西恩勉强肯定它确实是面包，只是硬得能打晕成年人。\n" +
                        "\n" +
                        "    强忍住饥饿，路西恩从板条箱里又翻出了打火石，失败了几次后，总算将火炉下的木材点燃，规规矩矩地开始烤起了面包。\n" +
                        "\n" +
                        "    “红烧肉、香辣鸡翅、粉蒸牛肉、宫保鸡丁……”一边烤着面包，路西恩一边念叨着美食，口水止不住地泛起。\n" +
                        "\n" +
                        "    黑面包稍微松软了一点，路西恩就忍不住地啃了起来，那恐怖的味道，简直就像是在咀嚼着木屑。\n" +
                        "\n" +
                        "    可路西恩还是狼吞虎咽地吃着，一边吃，一边忍不住哀叹：“要是每天都吃这样的食物，那还不如死了好，不行，一定要赚到钱，一定要摆脱这种贫民的生活。”\n" +
                        "\n" +
                        "    “要是能学会神术，成为主教和牧师……”漫无边际肖想中，路西恩想起了在教堂前看到的衣冠楚楚的主教和牧师，想起了那非人的力量和他们由此而来的高贵地位，忍不住怦然心动，“可是，我这样的状况，去教会不是找死吗？不知道还有没有其他获得非人超凡力量的途径？比如那个神恩？”\n" +
                        "\n" +
                        "    “如果不能获得这种力量，我学的东西，在这个世界派得上用场吗？”\n" +
                        "\n" +
                        "    止住了饥饿，路西恩开始认真地分析起自己的生存之路，但刚回想学过的知识时，路西恩突然发生，脑海里像是多了些什么东西。\n" +
                        "\n" +
                        "    仔细一察觉，路西恩忍不住睁大了双眼：“这不是图书馆里的书吗？它们也跟着穿越了？”\n" +
                        "\n" +
                        "    自己的脑海里竟然有整个综合图书馆的书籍，它们不像是记忆，而仿佛是投影，一本本分门别类的放好，供路西恩翻阅。\n" +
                        "\n" +
                        "    好奇的路西恩随意地翻阅着图书，可疑惑地发生，绝大部分图书竟然无法打开。";


                Thread.sleep(6000);
                CrawlNovelThrowable throwable = new CrawlNovelThrowable();
                throwable.chapterUrl = chapterUrl;
                throwable.novelUrl = novelId;
                if(chapterUrl.compareTo("2") == 0){
                    e.onNext(ret);
                }else{
                    e.onError(throwable);
                }
                //

                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());
    }

    protected CrawlNovel() {
        m_currEngineIndex = 0;
        loadEngines();
    }

    public void setCurrentCrawlNovelEngine(int index) {
        m_currEngineIndex = index;
    }

    private void loadEngines() {

    }


    private static ArrayList<ICrawlNovel> m_s_crawlNovelEngines = new ArrayList<ICrawlNovel>();
    private int m_currEngineIndex = -1;


}
