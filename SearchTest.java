public class SearchTest {
    public static void main(String[] args) {
        String str = null;
        int userid = 0;

        Search title = new SearchTitle();
        Search keyword = new SearchKeyword();
        Search tag = new SearchTag();

        title.searchMethod(str, userid);
        keyword.searchMethod(str, userid);
        tag.searchMethod(str, userid);
    }
}
