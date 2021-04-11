package net.imyeyu.pdl.ctrl;

import net.imyeyu.betterjava.Network;
import net.imyeyu.pdl.view.ViewAbout;

import java.net.URL;

public class About extends ViewAbout {

	public About() {
		blog.setOnAction(event -> {
			try {
				Network.openURIInBrowser(new URL("http://www.imyeyu.net").toURI());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		show();
	}
}
