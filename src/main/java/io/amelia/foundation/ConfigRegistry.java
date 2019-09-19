/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.data.ContainerBase;
import io.amelia.data.TypeBase;
import io.amelia.lang.ConfigException;
import io.amelia.support.IO;
import io.amelia.support.Lists;
import io.amelia.support.Objs;
import io.amelia.support.Streams;

public class ConfigRegistry
{
	private static String[] tlds = {"com", "org", "net", "int", "edu", "gov", "mil", "arpa", "[a-z]{2}", "xn--[a-z0-9]+", "asn.au", "com.au", "net.au", "id.au", "org.au", "edu.au", "gov.au", "csiro.au", "act.au", "nsw.au", "nt.au", "qld.au", "sa.au", "tas.au", "vic.au", "co.at", "or.at", "priv.at", "ac.at", "avocat.fr", "aeroport.fr", "veterinaire.fr", "co.hu", "film.hu", "lakas.hu", "ingatlan.hu", "sport.hu", "hotel.hu", "ac.nz", "co.nz", "geek.nz", "gen.nz", "kiwi.nz", "maori.nz", "net.nz", "org.nz", "school.nz", "cri.nz", "govt.nz", "health.nz", "iwi.nz", "mil.nz", "parliament.nz", "ac.il", "co.il", "org.il", "net.il", "k12.il", "gov.il", "muni.il", "idf.il", "ac.za", "gov.za", "law.za", "mil.za", "nom.za", "school.za", "net.za", "co.uk", "org.uk", "me.uk", "ltd.uk", "plc.uk", "net.uk", "sch.uk", "ac.uk", "gov.uk", "mod.uk", "mil.uk", "nhs.uk", "police.uk", "co.nl", "capetown", "durban", "joburg", "abudhabi", "asia", "doha", "dubai", "krd", "kyoto", "nagoya", "okinawa", "osaka", "ryukyu", "taipei", "tatar", "tokyo", "yokohama", "alsace", "amsterdam", "bcn", "barcelona", "bayern", "berlin", "brussels", "budapest", "bzh", "cat", "cologne", "corsica", "cymru", "eus", "frl", "gal", "gent", "hamburg", "helsinki", "irish", "ist", "istanbul", "koeln", "london", "madrid", "moscow (ru)", "nrw", "paris", "ruhr", "saarland", "scot", "stockholm", "swiss", "tirol", "vlaanderen", "wales", "wien", "zuerich", "boston", "miami", "nyc", "quebec", "vegas", "kiwi", "melbourne", "sydney", "lat", "rio", "aaa", "abarth", "abb", "aeg", "afl", "aig", "airbus", "airtel", "akdn", "alfaromeo", "allfinanz", "alstom", "americanexpress", "amex", "amica", "android", "anz", "aol", "apple", "aquarelle", "audi", "auspost", "axa", "azure", "barclaycard", "barclays", "bauhaus", "bbc", "bbt", "bbva", "bcg", "bentley", "bing", "bloomberg", "bms", "bmw", "bnl", "bnpparibas", "bosch", "bostik", "bradesco", "bridgestone", "brother", "bugatti", "cal", "calvinklein", "canon", "capitalone", "caravan", "cartier", "cba", "cbn", "cbs", "cern", "chanel", "chrome", "chrysler", "cisco", "citi", "citic", "clubmed", "commbank", "creditunion", "crs", "cuisinella", "datsun", "dell", "deloitte", "delta", "dhl", "dnp", "dodge", "dunlop", "dupont", "dvag", "edeka", "emerck", "epson", "ericsson", "esurance", "eurovision", "everbank", "extraspace", "fedex", "ferrari", "fiat", "firestone", "firmdale", "flickr", "flsmidth", "ford", "forex", "frogans", "fujitsu", "fujixerox", "gallup", "gbiz", "genting", "gle", "globo", "gmail", "gmo", "gmx", "godaddy", "goldpoint", "goodyear", "google", "guardian", "hdfc", "hdfcbank", "hisamitsu", "hitachi", "hkt", "honda", "honeywell", "hotmail", "hsbc", "htc", "hughes", "hyatt", "hyundai", "ibm", "ieee", "ikano", "imdb", "infiniti", "intel", "itv", "iveco", "jaguar", "java", "jeep", "jpmorgan", "kerryhotels", "kerrylogistics", "kerryproperties", "kfh", "kia", "kindle", "komatsu", "kpmg", "kred", "kuokgroup", "lacaixa", "ladbrokes", "lamborghini", "lancia", "lancome", "landrover", "latrobe", "lds", "lego", "lexus", "lidl", "lincoln", "linde", "lplfinancial", "lundbeck", "macys", "mango", "marriott", "maserati", "mcdonalds", "mckinsey", "metlife", "microsoft", "mini", "mit", "mitsubishi", "monash", "mormon", "movistar", "mtpc", "mutuelle", "nationwide", "neustar", "newholland", "nexus", "nhk", "nico", "nike", "nikon", "nissan", "nissay", "nokia", "norton", "nra", "ntt", "office", "omega", "oracle", "orange", "orientexpress", "otsuka", "ovh", "pamperedchef", "panasonic", "philips", "piaget", "pioneer", "play", "playstation", "pohl", "praxi", "prod", "pwc", "rexroth", "ricoh", "rocher", "sakura", "samsung", "sandvik", "sandvikcoromant", "saxo", "sbi", "sbs", "sca", "scb", "schmidt", "scjohnson", "seek", "shangrila", "sharp", "shell", "skype", "sncf", "sohu", "sony", "spiegel", "statebank", "statefarm", "statoil", "stc", "stcgroup", "suzuki", "swatch", "symantec", "taobao", "tatamotors", "telefonica", "toshiba", "toyota", "travelchannel", "tui", "tvs", "unicom", "uol", "ups", "vanguard", "verisign", "vig", "virgin", "visa", "vivo", "volkswagen", "volvo", "walmart", "walter", "weatherchannel", "williamhill", "windows", "wme", "wtc", "xbox", "xerox", "xfinity", "xperia", "yahoo", "yamaxun", "yandex", "youtube", "zappos", "zara", "zip", "zippo", "academy", "accountant", "accountants", "active", "actor", "ads", "adult", "aero", "agency", "airforce", "apartments", "app", "archi", "army", "associates", "attorney", "auction", "audible", "audio", "author", "auto", "autos", "baby", "band", "bar", "barefoot", "bargains", "baseball", "beauty", "beer", "best", "bestbuy", "bet", "bid", "bike", "bingo", "bio", "biz", "black", "blackfriday", "blockbuster", "blog", "blue", "boo", "book", "bot", "boutique", "box", "broker", "build", "builders", "business", "buy", "buzz", "cab", "cafe", "call", "cam", "camera", "camp", "cancerresearch", "capital", "cards", "care", "career", "careers", "cars", "case", "cash", "casino", "catering", "catholic", "center", "ceo", "cfd", "channel", "chat", "cheap", "christmas", "church", "circle", "city", "claims", "cleaning", "click", "clinic", "clothing", "cloud", "club", "coach", "codes", "coffee", "college", "community", "company", "computer", "condos", "construction", "consulting", "contact", "contractors", "cooking", "cool", "coop", "country", "coupon", "coupons", "courses", "credit", "creditcard", "cricket", "cruises", "dad", "dance", "date", "dating", "day", "deal", "deals", "degree", "delivery", "democrat", "dental", "dentist", "design", "dev", "diamonds", "diet", "digital", "direct", "directory", "discount", "diy", "doctor", "dog", "domains", "download", "duck", "earth", "eat", "eco", "education", "email", "energy", "engineer", "engineering", "equipment", "esq", "estate", "events", "exchange", "expert", "exposed", "express", "fail", "faith", "family", "fan", "fans", "farm", "fashion", "fast", "feedback", "film", "final", "finance", "financial", "fire", "fish", "fishing", "fit", "fitness", "flights", "florist", "flowers", "fly", "foo", "food", "foodnetwork", "football", "forsale", "forum", "foundation", "free", "frontdoor", "fund", "furniture", "fyi", "gallery", "game", "games", "garden", "gift", "gifts", "gives", "glass", "global", "gold", "golf", "gop", "graphics", "green", "gripe", "group", "guide", "guitars", "guru", "hair", "hangout", "health", "healthcare", "help", "here", "hiphop", "hiv", "hockey", "holdings", "holiday", "homegoods", "homes", "homesense", "horse", "host", "hosting", "hot", "house", "how", "ice", "info", "ing", "ink", "institute[65]", "insurance", "insure", "international", "investments", "jewelry", "jobs", "joy", "kim", "kitchen", "land", "latino", "law", "lawyer", "lease", "legal", "lgbt", "life", "lifeinsurance", "lighting", "like", "limited", "limo", "link", "live", "living", "loan", "loans", "locker", "lol", "lotto", "love", "luxe", "luxury", "makeup", "management", "market", "marketing", "markets", "mba", "media", "meet", "meme", "memorial", "men", "menu", "mint", "mobi", "mobily", "moe", "money", "mortgage", "motorcycles", "mov", "movie", "museum", "name", "navy", "network", "new", "news", "ngo", "ninja", "now", "off", "one", "ong", "onl", "online", "ooo", "open", "organic", "origins", "page", "partners", "parts", "party", "pay", "pet", "pharmacy", "photo", "photography", "photos", "physio", "pics", "pictures", "pid", "pin", "pink", "pizza", "place", "plumbing", "plus", "poker", "porn", "post", "press", "prime", "pro", "productions", "prof", "promo", "properties", "property", "protection", "qpon", "racing", "radio", "read", "realestate", "realty", "recipes", "red", "rehab", "ren", "rent", "rentals", "repair", "report", "republican", "rest", "review", "reviews", "rich", "rip", "rocks", "rodeo", "room", "rsvp", "run", "safe", "sale", "save", "school", "science", "secure", "security", "select", "services", "sex", "sexy", "shoes", "shop", "shopping", "show", "showtime", "silk", "singles", "webroot", "ski", "skin", "sky", "smile", "soccer", "social", "software", "solar", "solutions", "song", "space", "spot", "spreadbetting", "store", "stream", "studio", "study", "style", "sucks", "supplies", "supply", "support", "surf", "surgery", "systems", "talk", "tattoo", "tax", "taxi", "team", "tech", "technology", "tel", "tennis", "theater", "theatre", "tickets", "tips", "tires", "today", "tools", "top", "tours", "town", "toys", "trade", "trading", "training", "travel", "travelersinsurance", "trust", "tube", "tunes", "university", "vacations", "vet", "video", "villas", "vip", "vision", "vodka", "vote", "voting", "voyage", "wang", "watch", "watches", "weather", "webcam", "website", "wed", "wedding", "whoswho", "wiki", "win", "wine", "winners", "work", "works", "world", "wow", "wtf", "xxx", "xyz", "yoga", "you", "zero", "zone", "shouji", "tushu", "wanggou", "weibo", "xihuan", "maison", "epost", "gmbh", "haus", "immobilien", "jetzi", "kaufen", "kinder", "reise", "reisen", "schule", "versicherung", "desi", "shiksha", "casa", "immo", "moda", "voto", "bom", "passagens", "abogado", "gratis", "futbol", "hoteles", "juegos", "soy", "tienda", "uno", "viajes", "vuelos", "bar", "bank", "coop", "enterprises", "industries", "institute", "itda", "pharmacy", "pub", "realtor", "reit", "rest", "restaurant", "ventures", "example", "invalid", "local", "localhost", "onion", "test", "mail", "web", "eco", "med", "shop", "sport"};

	public static final ConfigData config = ConfigData.empty();
	private static boolean loaded = false;

	// TODO This LOADER is not thread-safe but if the application initialization works as intended, this shouldn't be an issue.
	public static final ConfigLoader LOADER = new ConfigLoader()
	{
		private ThreadLocal<ConfigData> configs = new ThreadLocal<>();

		@Override
		public ConfigData beginConfig() throws ConfigException.Error
		{
			if ( configs.get() != null )
				throw new ConfigException.Error( configs.get(), "There is existing configuration, it must be first be committed or destroyed!" );

			ConfigData config = ConfigData.empty();
			configs.set( config );
			return config;
		}

		/**
		 *
		 * @param type Future Use
		 *
		 * @throws ConfigException.Error
		 */
		@Override
		public void commitConfig( @Nonnull ConfigLoader.CommitType type ) throws ConfigException.Error
		{
			ConfigData config = configs.get();
			if ( config == null )
				throw new ConfigException.Error( null, "There is no configuration to commit. Use destroy() to use beginConfig()." );
			Streams.forEachWithException( config.getChildren(), child -> config.addChild( null, child, ContainerBase.ConflictStrategy.MERGE ) );
			configs.remove();
			loaded = true;
		}

		@Override
		public ConfigData config() throws ConfigException.Error
		{
			ConfigData config = configs.get();
			if ( config == null )
				throw new ConfigException.Error( null, "There is no configuration to commit, you must first use beginConfig()." );
			return config;
		}

		@Override
		public void destroy()
		{
			configs.remove();
		}

		@Override
		public boolean hasBeganConfig()
		{
			return configs.get() != null;
		}
	};

	/*
	 * We set default config values here for end-user reference, they're then saved to the config file upon load (if unset).
	 */
	static
	{
		try
		{
			config.setValueIfAbsent( ConfigKeys.WARN_ON_OVERLOAD );
			config.setValueIfAbsent( ConfigKeys.DEVELOPMENT_MODE );
			config.setValueIfAbsent( ConfigKeys.DEFAULT_BINARY_CHARSET );
			config.setValueIfAbsent( ConfigKeys.DEFAULT_TEXT_CHARSET );
		}
		catch ( ConfigException.Error e )
		{
			// Ignore
		}
	}

	public static void clearCache( @Nonnull Path path, @Nonnegative long keepHistory )
	{
		Objs.notNull( path );
		Objs.notNull( keepHistory );
		Objs.notNegative( keepHistory );

		try
		{
			if ( Files.isDirectory( path ) )
				Streams.forEachWithException( Files.list( path ), file -> {
					if ( Files.isDirectory( file ) )
						clearCache( file, keepHistory );
					else if ( Files.isRegularFile( file ) && IO.getLastModified( file ) < System.currentTimeMillis() - keepHistory * 24 * 60 * 60 )
						Files.delete( file );
				} );
		}
		catch ( IOException e )
		{
			Kernel.L.warning( "Exception thrown while clearing cache for directory " + path.toString(), e );
		}
	}

	public static void clearCache( @Nonnegative long keepHistory )
	{
		clearCache( Kernel.getPath( Kernel.PATH_CACHE ), keepHistory );
	}

	public static ConfigData getChild( String key )
	{
		return config.getChild( key );
	}

	public static ConfigData getChildOrCreate( String key )
	{
		return config.getChildOrCreate( key );
	}

	public static boolean isLoaded()
	{
		return loaded;
	}

	public static void save()
	{
		// TODO Save
	}

	/**
	 * Use with caution!
	 * ConfigRegistry will by default be marked as loaded once the ConfigLoader is committed for the first time.
	 *
	 * @param loaded the explicit value to set
	 */
	public static void setLoadedOverride( boolean loaded )
	{
		ConfigRegistry.loaded = loaded;
	}

	public static void setObject( String key, Object value ) throws ConfigException.Error
	{
		if ( value instanceof ConfigData )
			config.getChildOrCreate( key ).addChild( null, ( ConfigData ) value, ContainerBase.ConflictStrategy.OVERWRITE );
		else
			config.getChildOrCreate( key ).setValue( value );
	}

	private static void vendorConfig() throws IOException
	{
		// WIP Copies config from resources and plugins to config directories.

		Path configPath = Kernel.getPathAndCreate( Kernel.PATH_CONFIG );

		IO.extractResourceDirectory( "config", configPath, ConfigRegistry.class );
	}

	private ConfigRegistry()
	{
		// Static Access
	}

	public static class ConfigKeys
	{
		public static final TypeBase APPLICATION_BASE = new TypeBase( "app" );
		public static final TypeBase.TypeBoolean WARN_ON_OVERLOAD = new TypeBase.TypeBoolean( APPLICATION_BASE, "warnOnOverload", false );
		public static final TypeBase.TypeBoolean DEVELOPMENT_MODE = new TypeBase.TypeBoolean( APPLICATION_BASE, "developmentMode", false );
		public static final TypeBase CONFIGURATION_BASE = new TypeBase( "conf" );
		public static final TypeBase CONTENT_TYPES = new TypeBase( CONFIGURATION_BASE, "contentTypes" );
		public static final TypeBase EXT_TYPES = new TypeBase( CONFIGURATION_BASE, "extTypes" );
		public static final TypeBase.TypeString DEFAULT_BINARY_CHARSET = new TypeBase.TypeString( CONFIGURATION_BASE, "defaultBinaryCharset", "ISO-8859-1" );
		public static final TypeBase.TypeString DEFAULT_TEXT_CHARSET = new TypeBase.TypeString( CONFIGURATION_BASE, "defaultBinaryCharset", "UTF-8" );
		public static final TypeBase.TypeStringList TLDS = new TypeBase.TypeStringList( CONFIGURATION_BASE, "tlds", Lists.newArrayList( tlds ) );

		private ConfigKeys()
		{
			// Static Access
		}
	}
}
