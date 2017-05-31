package com.mobvoi.robot;

import android.app.Activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import com.mobvoi.robot.MainActivity.SpeechClientListenerImpl;

public class storyActivity extends Activity {
    static String[] content = {"從前有個可愛的小姑娘，誰見了都喜歡，但最喜歡她的是她的奶奶，簡直是她要什麼就給她什麼。 一次，奶奶送給小姑娘一頂用絲絨做的小紅帽，戴在她的頭上正好合適。 從此，姑娘再也不願意戴任何別的帽子，於是大家便叫她\"小紅帽\"。",
            " 一天，媽媽對小紅帽說：\"來，小紅帽，這裏有一塊蛋糕和一瓶葡萄酒，快給奶奶送去，奶奶生病了，身子很虛弱，吃了這些就會好一些的。趁着現在天還沒有熱，趕緊動身吧。在路上要好好走，不要跑，也不要離開大路，否則你會摔跤的，那樣奶奶就什麼也吃不上了。到奶奶家的時候，別忘了說'早上好'，也不要一進屋就東瞧西瞅。\" \"我會小心的。\"小紅帽對媽媽說，並且還和媽媽拉手作保證。",
            " 奶奶住在村子外面的森林裏，離小紅帽家有很長一段路。 小紅帽剛走進森林就碰到了一條狼。 小紅帽不知道狼是壞傢伙，所以一點也不怕它。 \"你好，小紅帽，\"狼說。 \"謝謝你，狼先生。\" \"小紅帽，這麼早要到哪裏去呀？\" \"我要到奶奶家去。\" \"你那圍裙下面有什麼呀？\" \"蛋糕和葡萄酒。昨天我們家烤了一些蛋糕，可憐的奶奶生了病，要吃一些好東西才能恢復過來。\" \"你奶奶住在哪裏呀，小紅帽？\" \"進了林子還有一段路呢。她的房子就在三棵大橡樹下，低處圍着核桃樹籬笆。你一定知道的。\"小紅帽說。 狼在心中盤算着：\"這小東西細皮嫩肉的，味道肯定比那老太婆要好。我要講究一下策略，讓她倆都逃不出我的手心。\"",
            "於是它陪着小紅帽走了一會兒，然後說：\"小紅帽，你看周圍這些花多麼美麗啊！幹嗎不回頭看一看呢？還有這些小鳥，它們唱得多麼動聽啊！你大概根本沒有聽到吧？林子裏的一切多麼美好啊，而你卻只管往前走，就像是去上學一樣。\" 小紅帽擡起頭來，看到陽光在樹木間來回跳蕩，美麗的鮮花在四周開放，便想：\"也許我該摘一把鮮花給奶奶，讓她高興高興。現在天色還早，我不會去遲的。\"她於是離開大路，走進林子去採花。 她每採下一朵花，總覺得前面還有更美麗的花朵，便又向前走去，結果一直走到了林子深處。 ",
            " 就在此時，狼卻直接跑到奶奶家，敲了敲門。 \"是誰呀？\" \"是小紅帽。\"狼回答，\"我給你送蛋糕和葡萄酒來了。快開門哪。\" \"你拉一下門栓就行了，\"奶奶大聲說，\"我身上沒有力氣，起不來。\"狼剛拉起門栓，那門就開了。",
            " 狼二話沒說就衝到奶奶的牀前，把奶奶吞進了肚子。 然後她穿上奶奶的衣服，戴上她的帽子，躺在牀上，還拉上了簾子。",
            " 可這時小紅帽還在跑來跑去地採花。 直到採了許多許多，她都拿不了啦，她纔想起奶奶，重新上路去奶奶家。 看到奶奶家的屋門敞開着，她感到很奇怪。",
            " 她一走進屋子就有一種異樣的感覺，心中便想：\"天哪！平常我那麼喜歡來奶奶家，今天怎麼這樣害怕？\"她大聲叫道：\"早上好！\"，可是沒有聽到回答。 她走到牀前拉開簾子，只見奶奶躺在牀上，帽子拉得低低的，把臉都遮住了，樣子非常奇怪。 \"哎，奶奶，\"她說，\"你的耳朵怎麼這樣大呀？\" \"爲了更好地聽你說話呀，乖乖。\" \"可是奶奶，你的眼睛怎麼這樣大呀？\"小"};
    static String[] imageNameAry = {"redhat1","redhat2","redhat3","redhat4","redhat5","redhat6","redhat7","redhat8"};
    static ImageView storyImg;
    public static Handler mainHandler = new Handler();
    private static Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        storyImg = (ImageView)findViewById(R.id.id_storyImg);
        storyImg.setImageResource(R.drawable.redhat1);
        SpeechClientListenerImpl.ttsSpeak(content[0],false);
        mContext = this;
    }

    public static void readNextContent(int index){
        String uri = "@drawable/" + imageNameAry[index];
        int imageResource = mContext.getResources().getIdentifier(uri, null, mContext.getPackageName());
        final Drawable image = mContext.getResources().getDrawable(imageResource);

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                storyImg.setImageDrawable(image);

            } // This is your code
        };
        mainHandler.post(myRunnable);

        SpeechClientListenerImpl.ttsSpeak(content[index],false);
    }

}
