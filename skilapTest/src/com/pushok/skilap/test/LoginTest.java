package com.pushok.skilap.test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.EditText;

import com.jayway.android.robotium.solo.Solo;
import com.pushok.skilap.R;
import com.pushok.skilap.activity.AccountsActivity;
import com.pushok.skilap.activity.AccsActivity;
import com.pushok.skilap.activity.DetailsActivity;
import com.pushok.skilap.activity.LoginActivity;

public class LoginTest extends ActivityInstrumentationTestCase2<AccountsActivity>{
	private Solo solo;
	private Context ctxTest;
	private Context ctxApp;
	private Activity loginActivity;
	private int activityTimeout = 20000;

	public LoginTest() {
		super("", AccountsActivity.class);
	}
	@Override
	public void setUp() throws Exception {
		ctxApp = getInstrumentation().getTargetContext().getApplicationContext();
		ctxTest = getInstrumentation().getContext();
		SharedPreferences settings = ctxApp.getSharedPreferences(AccountsActivity.PREFS_NAME, 0);
		settings.edit().clear().commit();
		solo = new Solo(getInstrumentation());
		getActivity();
		activityTimeout = Integer.valueOf(ctxTest.getString(com.pushok.skilap.tests.R.string.activityTimeout));
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
	}

	@SmallTest
	public void testLogin() throws Exception {
		assertTrue(solo.waitForActivity(LoginActivity.class, activityTimeout));
		solo.assertCurrentActivity("Error show login activity", LoginActivity.class);
		loginActivity = solo.getCurrentActivity();
		assertEquals("", solo.getEditText(0).getText().toString());
		assertEquals("", solo.getEditText(1).getText().toString());
		assertEquals("", solo.getEditText(2).getText().toString());
		solo.enterText((EditText) loginActivity.findViewById(R.id.etDomain), ctxTest.getString(com.pushok.skilap.tests.R.string.url));
		solo.enterText((EditText) loginActivity.findViewById(R.id.etUserName), ctxTest.getString(com.pushok.skilap.tests.R.string.user_name));
		solo.enterText((EditText) loginActivity.findViewById(R.id.etPassword), ctxTest.getString(com.pushok.skilap.tests.R.string.password));
		solo.clickOnButton(ctxApp.getString(R.string.Login));
		assertTrue(solo.waitForActivity(AccountsActivity.class, activityTimeout));
		solo.assertCurrentActivity("Error login", AccountsActivity.class);
	}

	@SmallTest
	public void testReLogin() throws Exception {
		testLogin();
		solo.waitForDialogToClose();
		solo.sendKey(Solo.MENU);
		solo.clickOnMenuItem(ctxApp.getString(R.string.Logout));
		
		assertTrue(solo.waitForActivity(LoginActivity.class, activityTimeout));
		solo.assertCurrentActivity("Error show relogin activity", LoginActivity.class);

		loginActivity = solo.getCurrentActivity();
		assertEquals(ctxTest.getString(com.pushok.skilap.tests.R.string.url), solo.getEditText(0).getText().toString());
		assertEquals("", solo.getEditText(1).getText().toString());
		assertEquals("", solo.getEditText(2).getText().toString());
		solo.enterText((EditText) loginActivity.findViewById(R.id.etUserName), ctxTest.getString(com.pushok.skilap.tests.R.string.user_name));
		solo.enterText((EditText) loginActivity.findViewById(R.id.etPassword), ctxTest.getString(com.pushok.skilap.tests.R.string.password));
		solo.clickOnButton(ctxApp.getString(R.string.Login));
		assertTrue(solo.waitForActivity(AccountsActivity.class, activityTimeout));
		solo.assertCurrentActivity("Error login", AccountsActivity.class);
	}

	@SmallTest
	public void testAccounts() throws Exception {
		testLogin();
		solo.waitForDialogToClose();
		assertTrue(solo.searchText("QIWI Bank"));
		assertTrue(solo.searchText("432.18"));
		assertTrue(solo.searchText("нал расходы"));
		assertTrue(solo.searchText("6.*992.00"));
		
		solo.clickOnText("QIWI Bank");;
		assertTrue(solo.waitForActivity(DetailsActivity.class, activityTimeout));
		
		assertTrue(solo.searchText("QIWI Bank\\s*-.*432.18"));
		assertNotNull(solo.getEditText("QIWI Bank"));
	}
	
	@LargeTest
	public void testDetails() throws Exception {
		testLogin();
		
		solo.waitForDialogToClose();
		solo.clickOnText("QIWI Bank");;
		assertTrue(solo.waitForActivity(DetailsActivity.class, activityTimeout));
		
		assertTrue(solo.searchText("QIWI Bank\\s*-.*432.18"));
		solo.clickOnText(ctxApp.getText(R.string.Recent).toString());;
		
		solo.waitForDialogToClose();
		assertTrue(solo.searchText("--\\s*Multiple\\s*--"));
		assertTrue(solo.searchText("14/12/11"));
		assertTrue(solo.searchText("нал расходы"));
		assertTrue(solo.searchText("1.*520.00"));
		assertTrue(solo.searchText("квартира2"));
		
		solo.clickOnText(ctxApp.getText(R.string.New).toString());
		solo.enterText(1, ctxTest.getString(com.pushok.skilap.tests.R.string.tr1Desc));
		solo.clickOnEditText(3);
		solo.enterText(3, ctxTest.getString(com.pushok.skilap.tests.R.string.spl1Price));
		solo.clickOnEditText(4);
		assertTrue(solo.waitForActivity(AccsActivity.class, activityTimeout));
		
		solo.clickOnText(ctxApp.getText(R.string.All).toString());;
		solo.scrollListToBottom(0);
		solo.clickOnText("нал расходы");
		assertTrue(solo.waitForActivity(DetailsActivity.class, activityTimeout));
		solo.assertCurrentActivity("Error login", DetailsActivity.class);
		assertTrue(solo.searchText("нал расходы"));
		assertEquals(ctxTest.getString(com.pushok.skilap.tests.R.string.spl1Price), solo.getEditText(5).getText().toString());
		assertEquals("нал расходы", solo.getEditText(4).getText().toString());
		solo.clickOnText(ctxApp.getText(R.string.Save).toString());;
		assertTrue(solo.waitForActivity(AccountsActivity.class, activityTimeout));
		solo.assertCurrentActivity("Error save trn", AccountsActivity.class);

		solo.waitForDialogToClose();
		assertTrue(solo.searchText("QIWI Bank"));
		assertTrue(solo.searchText("331.18"));
		assertTrue(solo.searchText("нал расходы"));
		assertTrue(solo.searchText("7.*093.00"));

		solo.clickOnText("QIWI Bank");;
		assertTrue(solo.waitForActivity(DetailsActivity.class, activityTimeout));
		
		solo.waitForDialogToClose();
		assertTrue(solo.searchText("QIWI Bank\\s*-.*331.18"));
		solo.clickOnText(ctxApp.getText(R.string.Recent).toString());;
		
		solo.searchText("нал расходы", 2);
		assertTrue(solo.searchText(ctxTest.getString(com.pushok.skilap.tests.R.string.spl1Price) + "0"));
		
		solo.clickOnText(ctxTest.getString(com.pushok.skilap.tests.R.string.spl1Price) + "0");
		
		assertTrue(solo.searchText("нал расходы"));
		assertEquals(ctxTest.getString(com.pushok.skilap.tests.R.string.tr1Desc), solo.getEditText(1).getText().toString());
		assertEquals(ctxTest.getString(com.pushok.skilap.tests.R.string.spl1Price), solo.getEditText(3).getText().toString());
		assertEquals(ctxTest.getString(com.pushok.skilap.tests.R.string.spl1Price), solo.getEditText(5).getText().toString());
		assertEquals("нал расходы", solo.getEditText(4).getText().toString());

		solo.clickOnEditText(3);
		solo.clearEditText(3);
		solo.enterText(3, ctxTest.getString(com.pushok.skilap.tests.R.string.spl2Price));
		solo.clickOnEditText(1);
		assertEquals(ctxTest.getString(com.pushok.skilap.tests.R.string.spl2Price), solo.getEditText(5).getText().toString());

		solo.clickOnText(ctxApp.getText(R.string.Save).toString());
		assertTrue(solo.waitForActivity(AccountsActivity.class, activityTimeout));
		solo.assertCurrentActivity("Error save trn", AccountsActivity.class);

		assertTrue(solo.searchText("QIWI Bank"));
		assertTrue(solo.searchText("330.18"));
		assertTrue(solo.searchText("нал расходы"));
		assertTrue(solo.searchText("7.*094.00"));
	}
}
