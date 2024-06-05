public abstract class Search {
    protected abstract void search(String s, int id);

    public final void searchMethod(String s, int id) {
        search(s, id);
    }
}