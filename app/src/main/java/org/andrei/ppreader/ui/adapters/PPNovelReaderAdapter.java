package org.andrei.ppreader.ui.adapters;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import org.andrei.ppreader.R;
import org.andrei.ppreader.service.CrawlNovel;
import org.andrei.ppreader.service.CrawlNovelService;
import org.andrei.ppreader.service.CrawlTextResult;
import org.andrei.ppreader.service.PPNovel;
import org.andrei.ppreader.service.PPNovelChapter;
import org.andrei.ppreader.ui.PPNovelTitleCenterBoldSpan;


import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static org.andrei.ppreader.ui.Utils.autoSplitText;
import static org.andrei.ppreader.ui.Utils.half2full;

public class PPNovelReaderAdapter extends PagerAdapter {


    String title = "\n第一章 今天开始做王子\n\n";
    String examp =
            "浓烟滚滚，每吸一口都发出破烂风箱般粗重的声音，像是在灼烧着咽喉和肺部，夏风的意识很快就模糊起来。\n" +
            "\n" +
            "　　“不能，不能睡过去，会死的。”\n" +
            "\n" +
            "　　“清醒，必须清醒！”\n" +
            "\n" +
            "　　……\n" +
            "\n" +
            "　　无边无际的赤红颜色霍地暗淡，最深、最沉的漆黑浮现，夏风仿佛溺水的人般，挣扎着想要抓住每一个可以抓住的东西来帮助自己改变这无力飘荡的状态，摆脱这无法言喻的黑暗。\n" +
            "\n" +
            "　　忽然，前面亮起一点红彤彤的光芒，像是清晨的太阳升起。\n" +
            "\n" +
            "　　在它的照耀下，夏风觉得自己恢复了一点力量，然后拼了命似地向着红光靠拢。\n" +
            "\n" +
            "　　当夏风借助照耀，真切地迈出这一步后，光芒越来越亮，由赤红转为纯白，将黑暗洞彻地支离破碎，瞬间消褪。\n" +
            "\n" +
            "　　“呼。”夏风猛地坐起，大口大口地喘着气，自己竟然梦到了一场可怕的火灾，而且在大火烧来之前，睡梦中的自己就因为吸入过多的浓烟而陷入昏迷，只能模糊、绝望地等待着火苗蔓延过来，就与以前几次鬼压床一样，明明知道在做梦，可想要挣脱，却没有力气，无法控制。\n" +
            "\n" +
            "　　这场梦异常地真实，让夏风心有余悸，加上并没有感觉到大火的存在，所以他呆滞地坐着，好久没有回过神来。\n" +
            "\n" +
            "　　随着急速跳动的心脏慢慢平稳，夏风精神凝聚，想起自己是在学校综合图书馆的通宵阅览室赶毕业论文，心中自嘲一句：“最近几天都是这种不规律的熬夜生活，难怪会做这么真实的噩梦。”\n" +
            "\n" +
            "　　可等夏风看向眼前，准备收拾参考书回寝室时，陌生而超乎想象的一幕，像是巨锤般击中了他的脑海，让他猛然呆住，一片空白。\n" +
            "\n" +
            "　　面前已经没有图书馆的漂亮木质书桌，没有了堆得乱七八糟的参考书，没有了准备输入电脑的论文草稿，只有一张黑乎乎、边角破烂发毛的毯子，而这张毯子就盖在自己身上。\n" +
            "\n" +
            "　　自己坐着的地方并不是图书馆的靠背椅子，而是一张狭窄的木床。\n" +
            "\n" +
            "　　“这是哪里？！”\n" +
            "\n" +
            "　　夏风虽然性格有些内向腼腆，反应不够快，但此时再怎么样都发现了种种不对之处：就算是真的着了火，自己被人送到了医院，这里也不像是医院！\n" +
            "\n" +
            "　　他内心一紧，慌忙看向四周，并且反射般忙不迭地往旁边站起。\n" +
            "\n" +
            "　　刚踩在地上，夏风就感觉到一阵极端的虚弱和眩晕，双脚一软，就要斜斜倒下。\n" +
            "\n" +
            "　　夏风慌忙伸出手，撑在床上，稳住自己，其脸色煞白、心神不宁，刚才这惊鸿一瞥，已经是将周围完全打量了一遍。\n" +
            "\n" +
            "　　一间破旧、狭窄的小屋，屋中除了身旁这张木床之外，只有一张随时都可能会散架的木桌，两张还算完好的凳子，一个破了洞的板条箱，而摇摇欲坠的木门另外一边，则有一个看不出本来颜色的火炉和吊在火炉上的瓦罐，火炉下的木柴已经不知熄灭了多久，没有半点热度传出。\n" +
            "\n" +
            "　　一切都是如此的陌生，夏风根本无法判断自己身在哪里，而不停涌现的虚弱感更是让夏风脑海一片混乱：\n" +
            "\n" +
            "　　“这到底是哪里？！”\n" +
            "\n" +
            "　　“身体就像是才大病了一场，与读高中那次肺炎刚刚消去的感觉很像。”\n" +
            "\n" +
            "　　……\n" +
            "\n" +
            "　　无数的想法在脑海中回荡，可夏风从来没有遇见过这种奇怪到了极点的事情，有点内向的性格让他一时竟然不知接下来该怎么做，浓浓的恐慌急速发酵着。\n" +
            "\n" +
            "　　唯一值得感谢的是，并没有任何讨厌的事物出现，让夏风能够习惯性地深呼吸了几次，慢慢地平复着恐慌，这时小屋外远远传来高喊声：\n" +
            "\n" +
            "　　“烧女巫了！阿得让教堂要烧女巫了！”\n" +
            "\n" +
            "　　“大家快去！”\n" +
            "\n" +
            "　　“烧死那该死的、邪恶的女巫！”\n" +
            "\n" +
            "　　恐惧和兴奋两种截然相反的情绪在奇怪的口音中明显地流露，夏风被打断了恐慌，好奇起来，自语了一句：“女巫？这里究竟是什么世界？”\n" +
            "\n" +
            "　　作为一名喜好小说的成年人，那种不好的预感在夏风心里隐隐产生，可还没来得及深思，哐当一声，可怜的破烂木门就被人一下撞开，一名十二三岁的男孩冲了进来。\n" +
            "\n" +
            "　　“路西恩大哥……”留着褐色短发，穿着齐膝亚麻上衣的男孩看到站在床边的夏风，意外而惊喜，“你醒了？”\n" +
            "\n" +
            "　　夏风看着这不同于现代风格的衣服，麻木地点着头，脑子里乱糟糟地产生一个荒谬的念头：“路西恩、女巫、教堂、烧死，莫非我真的穿越了，而且还是穿越到了欧洲中世纪猎杀女巫的黑暗时代？”\n" +
            "\n" +
            "　　事情总是朝着坏的方向发展，墨菲定律冷酷地提醒着夏风，男孩的发色，穿着的脏旧亚麻衣服，都在印证着这一点，至于男孩说的语言，夏风身体本能地可以听懂，也似乎能够运用，只不过距离语言学家还有很遥远距离的他，无法判断是哪种语言。\n" +
            "\n" +
            "　　见夏风魂不守舍，脸上有着几道黑灰污痕的小男孩并不奇怪：“妈妈总是不肯相信我，半夜还偷偷哭泣，哭得眼睛都肿了，一直念叨着可怜的小伊文斯，就像路西恩大哥你已经被埋在了墓园里面一样。”\n" +
            "\n" +
            "　　“爸爸被吵得没办法，一大早就找西蒙家那坏小子送信去维恩爵士的庄园，让哥哥想办法回来一次，他已经是见习的骑士侍从了，善堂的医生可不敢在他面前坚持那离谱的、好笑的价钱。”\n" +
            "\n" +
            "　　说起自己成为见习骑士侍从的哥哥，小男孩下巴微抬，有一种由衷的自豪。\n" +
            "\n" +
            "　　“不过，现在好了，他们输了，我是对的，路西恩大哥你怎么可能会有事！”\n" +
            "\n" +
            "　　边说，他边拉着夏风的手臂：“走，路西恩大哥，快去教堂广场看烧女巫，就是那个害得你被教堂守卫抓去审问了一夜的可恶女巫！”\n" +
            "\n" +
            "　　适逢大变，正想自己安静思考一下人生的夏风，本来不想跟着这小男孩去凑热闹，而且直接烧死一位活人，实在是自诩为还算良善的夏风无法接受的事情，既然自己无力阻止，那就还是不要目睹得好，可小男孩最后的那句话，却让夏风一下惊住：“这女巫和我有牵连？”\n" +
            "\n" +
            "　　于是夏风想法一变，收敛住惊讶，任由小男孩拉住，跌跌撞撞往阿得让教堂奔去。\n" +
            "\n" +
            "　　一路之上，夏风抓住机会打量着前往阿得让教堂的人们。\n" +
            "\n" +
            "　　天气比较暖和，男子基本穿着短窄衣袖的亚麻上衣，同色长裤和无跟鞋，女的则是乏味单一的长裙，裙子上往往缝合着一个大大的口袋，共同之处是简朴和破旧。\n" +
            "\n" +
            "　　大部分上的褐发棕瞳中掺杂金发、红发、黑发、碧眼、红瞳、蓝眼等，五官深刻，很有立体感。\n" +
            "\n" +
            "　　“难道真的是中世纪？”夏风看了看自身，一样的亚麻短上衣，一样的长裤，一样的无跟鞋。\n" +
            "\n" +
            "　　从破旧、低矮的贫民屋子聚集区出来没多久，就看到一座不大但庄严气派的教堂，半圆拱顶高耸，一个巨大的白色十字架钉在上面，下方的窗户则非常窄小。\n" +
            "\n" +
            "　　广场上已经围着很多人，小男孩拉着夏风左钻右闪，不停往前挤着，引得不少人怒气勃勃地看过来，只是碍于这里是教堂广场，不敢揍这两个混蛋。\n" +
            "\n" +
            "　　挤了没多久，夏风眼前豁然开朗，原来已经是挤到了最前面一层。";

    public PPNovelReaderAdapter(Fragment parent){
        m_parent = parent;
        PPNovelTextPage page = new PPNovelTextPage();
        page.text = examp;
        page.title = title;
        m_pages.add(page);
    }

    @Override
    public int getCount() {
        return m_pages.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position){
        View v = m_parent.getActivity().getLayoutInflater().inflate(R.layout.view_ppnovel_reader,null);
        final TextView tv = (TextView)v.findViewById(R.id.novel_reader_text);
        final PPNovelTextPage page = m_pages.get(position);
        if(page.isSplited){
            if(page.offset == 0){
                setZeroOffsetPageText(tv,page.text);
            }
            else{
                tv.setText(page.text);
            }
        }
        else{
            final String text = half2full(page.text);
            final int pos = position;
            tv.setText(text);
            tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    tv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    reallocateText(tv,text,page.title,pos);
                }
            });
        }
        container.addView(v);
        return v;
    }

    @Override
    public int getItemPosition(Object object) {
        if (m_bNeedUpdate) {
            m_bNeedUpdate = false;
            return POSITION_NONE;
        } else {
            return super.getItemPosition(object);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    private void reallocateText(final TextView tv,final String text, final String title,final int pos ){
        String newText = title +  autoSplitText(tv,text);
        setZeroOffsetPageText(tv,newText);
        tv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                splitText(tv,pos);
            }
        });
    }
    private void setZeroOffsetPageText(final TextView tv,final String text){
        SpannableString sp = new SpannableString(text);
        int end = text.indexOf('\n',1) ;
        float fontSize =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, tv.getResources().getDisplayMetrics());
        float padding =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, tv.getResources().getDisplayMetrics());
        sp.setSpan(new PPNovelTitleCenterBoldSpan(fontSize,padding),1,end+1,0);
        tv.setText(sp);
    }

    private void splitText(TextView tv,int pos){
        String text = tv.getText().toString();
        float height = tv.getHeight();
        int count = tv.getLineCount();
        float txtHeight = 0;
        float endLineBottomMargin = tv.getLineSpacingExtra();
        int offset  = 0;
        int begin = 0;
        Rect rc = new Rect();
        for(int i = 0; i < count; i++){
            tv.getLineBounds(i,rc);
            txtHeight += rc.height();
            if(txtHeight >= height || i == count - 1){
                PPNovelTextPage page = null;
                if(begin == 0){
                    //the height of the title should be greater than the normal line, because the title font size is more bigger. But the system thinks they are the sane height, we should -1 ,
                    // otherwise, the text will be beyond the page
                    page = m_pages.get(pos);
                    i --;
                }
                else{
                    if(txtHeight - endLineBottomMargin > height ){
                        i--;
                    }
                    page = new PPNovelTextPage();
                    m_pages.add(pos+offset,page);
                }
                page.isSplited = true;
                page.offset = offset;
                offset++;
                int end = tv.getLayout().getLineEnd(i);
                page.text = text.substring(begin, end);
                if(i != count - 1){
                    begin = tv.getLayout().getLineStart(i + 1);
                    txtHeight = 0;
                }
            }
        }
        m_bNeedUpdate = true;
        this.notifyDataSetChanged();
    }

    private void fetchChapterText(PPNovelChapter chapter){
        m_fetchList.add(chapter);
        if(!m_bRunning){
            fetchChapterTextProc();
        }
    }

    private void fetchChapterTextProc(){
        if(m_crawlNovel == null){
            m_crawlNovel = CrawlNovelService.instance().builder(m_novel.engineIndex);
        }
        if(m_fetchList.size() == 0){
            return;
        }
        m_bRunning = true;
        PPNovelChapter chapter = m_fetchList.remove(0);
        m_crawlNovel.fetchNovelText(m_novel.chapterUrl,chapter.url).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<CrawlTextResult>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(CrawlTextResult value) {

            }

            @Override
            public void onError(Throwable e) {
                fetchChapterTextProc();
                if(m_fetchList.size() == 0){
                    m_bRunning = false;
                }
            }

            @Override
            public void onComplete() {
                fetchChapterTextProc();
                if(m_fetchList.size() == 0){
                    m_bRunning = false;
                }
            }
        });
    }



    private CrawlNovel m_crawlNovel = null;
    private PPNovel m_novel;
    private ArrayList<PPNovelChapter> m_fetchList = new ArrayList<PPNovelChapter>();
    private boolean m_bRunning = false;
    private Fragment m_parent;
    private ArrayList<PPNovelTextPage> m_pages = new ArrayList<PPNovelTextPage>();
    private boolean m_bNeedUpdate = false;

    class PPNovelTextPage{
        public String text;
        public int offset = 0;
        public boolean isSplited = false;
        public String title;
        int chapterIndex = 0;
    }

}
