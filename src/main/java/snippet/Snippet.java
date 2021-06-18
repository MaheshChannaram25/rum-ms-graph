package snippet;

import java.util.Collections;

import com.microsoft.graph.auth.publicClient.UsernamePasswordProvider;

public class Snippet {
	UsernamePasswordProvider authProvider = new UsernamePasswordProvider(
			"39c8c25a-dea3-4166-afc2-56351523951b",
			Collections.singletonList("https://graph.microsoft.com/.default"),
            "admin.sugarcrm@tsa-solutions.com",
            "CI6Ikp@XVCJ9");
}

