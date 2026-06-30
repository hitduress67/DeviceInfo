package $PKG;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class InfoCard extends LinearLayout {
    
    private TextView labelView;
    private TextView valueView;
    private CardView card;
    
    public InfoCard(Context context) {
        super(context);
        init(context);
    }
    
    public InfoCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_info_card, this, true);
        card = findViewById(R.id.infoCard);
        labelView = findViewById(R.id.infoLabel);
        valueView = findViewById(R.id.infoValue);
    }
    
    public void setLabel(String label) {
        labelView.setText(label);
    }
    
    public void setValue(String value) {
        valueView.setText(value);
    }
    
    public void setCardBackgroundColor(int color) {
        card.setCardBackgroundColor(color);
    }
}
