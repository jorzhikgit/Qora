package qora.web.blog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import qora.naming.Name;
import qora.web.Profile;
import utils.DateTimeFormat;
import utils.LinkUtils;
import utils.Pair;
import webserver.WebResource;
import database.DBSet;

/**
 * This is the representation of an entry in a blog.
 * 
 * @author Skerberus
 *
 */
public class BlogEntry {

	private String titleOpt;
	private String description;
	private String nameOpt;
	private long time;
	private final String creator;
	private String 	avatar = "img/qora-user.png";
	private final Profile profileOpt;

	private List<String> imagelinks = new ArrayList<String>();
	private String signature;
	private boolean isLiking = false;
	private List<String> likingUser = new ArrayList<>();
	private List<String> sharingUser = new ArrayList<>();
	private String blogname;
	/**
	 * Only Set in case of a shared post (if I share your post I am the share author)
	 */
	private String shareAuthorOpt = null;

	public BlogEntry(String titleOpt, String description, String nameOpt,
			long timeOpt, String creator, String signature, String blogname) {
		this.titleOpt = titleOpt;
		this.signature = signature;
		this.setBlogname(blogname);
		this.description = description.replaceAll("\n", "<br/>");
		this.description = Jsoup.clean(this.description, Whitelist.basic());
		handleImages();
		handleLinks();
		this.nameOpt = nameOpt;
		profileOpt = Profile.getProfileOpt(nameOpt);
		addAvatar();
		this.time = timeOpt;
		this.creator = creator;
		
	}

	private void addAvatar() {
		if (nameOpt != null) {
			Name name = DBSet.getInstance().getNameMap().get(nameOpt);
			Profile profile = Profile.getProfileOpt(name);
			if(profile != null)
			{
				String avatarOpt = profile.getAvatarOpt();
				if (avatarOpt != null && StringUtils.isNotBlank(avatarOpt)) {
					avatar = avatarOpt;
				}
			}
		}
	}

	private void handleImages() {
		Pattern pattern = Pattern.compile(Pattern.quote("[img]") + "(.+)" + Pattern.quote("[/img]"));
		Matcher matcher = pattern.matcher(description);
		while (matcher.find()) {
			String url = matcher.group(1);
			imagelinks.add(url);
			description = description.replace(matcher.group(), getImgHtml(url));
		}

	}
	
	
	private String getImgHtml(String url)
	{
		try {
			String template = WebResource.readFile("web/imgtemplate",
					StandardCharsets.UTF_8);
			return template.replace("{{url}}", url);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private void handleLinks() {
		List<Pair<String, String>> linkList = LinkUtils
				.createHtmlLinks(LinkUtils.getAllLinks(description));

		for (Pair<String, String> link : linkList) {
			String originalLink = link.getA();
			String newLink = link.getB();
			if(!imagelinks.contains(originalLink))
			{
				description = description.replace(originalLink, newLink);
			}
		}

	}

	public String getTitleOpt() {
		return titleOpt;
	}

	public String getDescription() {
		return description;
	}

	public String getNameOpt() {
		return nameOpt;
	}

	/**
	 * Returns name if name not null creator else
	 * 
	 * @return
	 */
	public String getNameOrCreator() {
		return nameOpt == null ? creator : nameOpt;
	}

	public long getTime() {
		return time;
	}
	
	public void setTime(long time)
	{
		this.time = time;
	}

	public String getCreator() {
		return creator;
	}

	public String getCreationTime() {
		return DateTimeFormat.timestamptoString(time);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getAvatar() {
		return avatar;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("nameOrCreator", this.getNameOrCreator());
		json.put("creationTime", this.getCreationTime());
		json.put("titleOpt", this.getTitleOpt());
		json.put("description", this.getDescription());
		json.put("avatar", this.getAvatar());
		
		return json;	
	}

	public Profile getProfileOpt() {
		return profileOpt;
	}

	public String getSignature() {
		return signature;
	}

	public boolean isLiking() {
		return isLiking;
	}

	public void setLiking(boolean isLiking) {
		this.isLiking = isLiking;
	}
	
	public void addLikingUser(String user)
	{
		if(!likingUser.contains(user))
		{
			likingUser.add(user);
		}
	}
	
	public int getLikes()
	{
		return likingUser.size();
	}
	
	public void addSharedUser(String user)
	{
		if(!sharingUser.contains(user))
		{
			sharingUser.add(user);
		}
	}
	
	public int getShares()
	{
		return sharingUser.size();
	}

	public String getShareAuthorOpt() {
		return shareAuthorOpt;
	}

	public void setShareAuthor(String shareAuthor) {
		this.shareAuthorOpt = shareAuthor;
	}

	public String getBlognameOpt() {
		return blogname;
	}

	public void setBlogname(String blogname) {
		this.blogname = blogname;
	}
	

}