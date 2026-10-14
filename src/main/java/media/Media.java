package media;
import slideshow.LinkedChain;

public abstract class Media {
    // ~ Fields ................................................................
    private String name;
    private String date;
    private LinkedChain<String> tags;

    // ~ Constructors ..........................................................
    public Media(String name, String date)
    {
        this.name = name;
        this.date = date;
        tags = new LinkedChain<String>();
    }


    // ~Public Methods ........................................................
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getDate()
    {
        return date;
    }


    public void setDate(String date)
    {
        this.date = date;
    }


    private String normalizeTag(String tag)
    {

        String normTag = tag;
        normTag = normTag.trim();
        normTag = normTag.replaceAll("\\s+", " ");
        normTag = normTag.toLowerCase();

        return normTag;
    }


    public void addTag(String tag)
    {
        if (tag == null)
        {
            throw new IllegalArgumentException("Tag cannot be empty.");
        }

        String normTag = normalizeTag(tag);

        if (normTag.equals(""))
        {
            throw new IllegalArgumentException("Tag cannot be empty.");
        }

        if (!hasTag(normTag))
        {
            tags.add(normTag);
        }
    }


    public boolean hasTag(String tag)
    {
        if (tag == null)
        {
            throw new IllegalArgumentException("Tag cannot be empty.");
        }

        String normTag = normalizeTag(tag);

        if (tags.contains(normTag))
        {
            return true;
        }
        
        return false;
    }


    public boolean removeTag(String tag)
    {
        if (tag == null)
        {
            return false;
        }

        if (hasTag(tag))
        {
            tags.remove(tag);
            return true;
        }

        return false;
    }


    public LinkedChain<String> getTags()
    {
        return this.tags;
    }
    
    public String toString() {
        return name + " | " + date + " | tags: " + tags.toString();
    }


    public abstract void draw();


    public abstract void getDisplayDuration();

}
