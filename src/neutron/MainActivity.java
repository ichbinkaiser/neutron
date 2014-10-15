package neutron;

import core.neutron.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends Activity 
{
	@Override
	public void onCreate(Bundle savedinstancestate) 
	{
		super.onCreate(savedinstancestate);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
        GameActivity.getResourceManager().doCleanup();
	}

	public void startGame(View view)
	{
		Intent gameIntent = new Intent(this, GameActivity.class);
		EditText balls;
		CheckBox solo;
		solo = (CheckBox) findViewById(R.id.checkBox1); // solo game checkbox
		balls = (EditText) findViewById(R.id.editText1); // retrieve ball count from user
		
		if (balls.getText().length() > 0)
			gameIntent.putExtra("BALLS_COUNT", Integer.parseInt(balls.getText().toString()));
		else
			gameIntent.putExtra("BALLS_COUNT", -1); 
		
		if (solo.isChecked())
			gameIntent.putExtra("SOLO_GAME", true);
		
		startActivity(gameIntent);
	}
}
