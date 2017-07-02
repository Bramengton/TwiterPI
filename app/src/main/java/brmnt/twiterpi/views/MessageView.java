package brmnt.twiterpi.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Pattern;

/**
 * @author by Bramengton
 * @date 01.07.17.
 */
public class MessageView extends TextView{
    public MessageView(Context context) {
        super(context);
    }

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public MessageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    public void setPressed(boolean pressed) {
        // If the parent is pressed, do not set to pressed.
        if (pressed && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    }

    @Override
    public boolean hasFocusable() {
        return false;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        this.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void showLinks(PatternEditableBuilder.SpannableClickedListener listener){
        new PatternEditableBuilder()
                .addPattern(Pattern.compile("\\@(\\w+)"), Color.GREEN, listener)
                .addPattern(Pattern.compile("\\#(\\w+)"), Color.CYAN, listener)
                .addPattern(Patterns.WEB_URL, new PatternEditableBuilder.SpannableClickedListener() {
                    @Override
                    public void onSpanClicked(String text) {
                        openUrl(Uri.parse(text));
                    }
                }).into(this);
    }

    private void openUrl(Uri uri){
        this.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }


}
