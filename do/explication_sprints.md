# Explication detaillee des Sprints — Mini Framework Java

---

## Sprint 0 — FrontServlet : intercepter toutes les URLs

### Quoi ?

En Java web classique, chaque URL a sa propre Servlet :

```
/employe/liste  ->  ListeServlet.java
/employe/fiche  ->  FicheServlet.java
/connexion      ->  ConnexionServlet.java
```

Le Sprint 0 remplace tout ca par une seule Servlet qui intercepte toutes les URLs.

### Comment ?

Le `web.xml` de l'app de test declare la Servlet avec le pattern `/*` :

```xml
<servlet>
    <servlet-name>FrontServlet</servlet-name>
    <servlet-class>itu.webdynamique.framework.FrontServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>FrontServlet</servlet-name>
    <url-pattern>/*</url-pattern>
</servlet-mapping>
```

`FrontServlet` centralise `doGet` et `doPost` dans une seule methode `processRequest` :

```java
public class FrontServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String uri = request.getRequestURI();
        out.println("URI complete : " + uri);

        String[] splitUri = uri.split("/");
        String dernierSegment = "";
        if (splitUri.length > 0) {
            dernierSegment = splitUri[splitUri.length - 1];
        }
        out.println("Dernier segment : " + dernierSegment);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
```

### Exemple

URL tapee : `localhost:8080/sprint0/employe/liste`

Resultat dans le navigateur :
```
URI complete : /sprint0/employe/liste
Dernier segment : liste
```

---

## Sprint 1 — @Controller : scanner de package

### Quoi ?

Le framework doit trouver automatiquement quelles classes sont des controleurs, sans qu'on les liste manuellement nulle part.

### Comment ?

Creation de l'annotation `@Controller` :

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {
}
```

`RetentionPolicy.RUNTIME` est indispensable : sans ca, l'annotation disparait apres compilation et le framework ne peut pas la lire.

Declaration du package a scanner dans `web.xml` :

```xml
<init-param>
    <param-name>package_controllers</param-name>
    <param-value>itu.webdynamique.app.controller</param-value>
</init-param>
```

`PackageScanner` parcourt ce package :

```java
public class PackageScanner {

    public static List<Class<?>> findByPackage(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        if (resource == null) return classes;
        File directory = new File(resource.toURI());
        for (File file : findAll(directory)) {
            String absolutePath = file.getAbsolutePath().replace('\\', '/');
            int idx = absolutePath.indexOf(path);
            String className = absolutePath.substring(idx, absolutePath.length() - 6).replace('/', '.');
            classes.add(Class.forName(className));
        }
        return classes;
    }

    private static List<File> findAll(File directory) {
        List<File> result = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) return result;
        for (File file : files) {
            if (file.isDirectory()) result.addAll(findAll(file));
            else if (file.getName().endsWith(".class")) result.add(file);
        }
        return result;
    }
}
```

`init()` dans `FrontServlet` filtre les classes avec `@Controller` :

```java
@Override
public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
        String packageToScan = config.getInitParameter("package_controllers");
        List<Class<?>> allClasses = PackageScanner.findByPackage(packageToScan);
        for (Class<?> cls : allClasses) {
            if (cls.isAnnotationPresent(Controller.class)) {
                System.out.println("Controleur detecte : " + cls.getName());
            }
        }
    } catch (Exception e) {
        throw new ServletException("Erreur scan", e);
    }
}
```

### Exemple

```java
@Controller
public class EmpController {
}
```

Console Tomcat au demarrage :
```
Controleur detecte : itu.webdynamique.app.controller.EmpController
```

---

## Sprint 2 — @UrlMapping : HashMap URL vers methode

### Quoi ?

On veut lier chaque URL a une methode precise d'un controleur. Une meme classe peut avoir plusieurs methodes, chacune liee a une URL differente.

### Comment ?

Creation de l'annotation `@UrlMapping` niveau methode :

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UrlMapping {
    String value();
}
```

Creation de la classe `Mapping` qui stocke classe et methode associees a une URL :

```java
public class Mapping {
    private String className;
    private String methodName;

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName()  { return className; }
    public String getMethodName() { return methodName; }
}
```

Remplissage du HashMap dans `init()` :

```java
private HashMap<String, Mapping> urlMappingMap = new HashMap<>();

for (Class<?> cls : allClasses) {
    if (!cls.isAnnotationPresent(Controller.class)) continue;
    for (Method method : cls.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(UrlMapping.class)) continue;
        String url = method.getAnnotation(UrlMapping.class).value();
        urlMappingMap.put(url, new Mapping(cls.getName(), method.getName()));
    }
}
```

Recherche dans `processRequest` :

```java
String requestedUrl = request.getRequestURI().substring(request.getContextPath().length());

if (urlMappingMap.containsKey(requestedUrl)) {
    Mapping mapping = urlMappingMap.get(requestedUrl);
    out.println("Classe  : " + mapping.getClassName());
    out.println("Methode : " + mapping.getMethodName());
} else {
    out.println("URL inconnue : " + requestedUrl);
}
```

### Exemple

```java
@Controller
public class EmpController {

    @UrlMapping("/employe/liste")
    public void afficherListe() { }

    @UrlMapping("/employe/fiche")
    public void afficherFiche() { }
}
```

HashMap construit au demarrage :
```
"/employe/liste" -> (EmpController, afficherListe)
"/employe/fiche" -> (EmpController, afficherFiche)
```

URL tapee : `localhost:8080/sprint0/employe/liste`
```
Classe  : itu.webdynamique.app.controller.EmpController
Methode : afficherListe
```

---

## Sprint 3 — VerbUrl, equals/hashCode, reflexion, Prototype

### Quoi ?

Deux problemes a regler :

**Probleme 1 :** la cle du HashMap est juste une URL. Impossible d'avoir `/employe/liste`
en GET et en POST pour faire des choses differentes.

**Probleme 2 :** on affiche juste le nom de la methode sans l'executer reellement.

### Comment — Probleme 1 : classe VerbUrl

Au lieu d'une String comme cle, on cree une classe avec deux attributs :

```java
public class VerbUrl {
    private String url;
    private String methodeHttp;

    public VerbUrl(String url, String methodeHttp) {
        this.url = url;
        this.methodeHttp = methodeHttp.toUpperCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VerbUrl autre = (VerbUrl) obj;
        return this.url.equals(autre.url) &&
               this.methodeHttp.equals(autre.methodeHttp);
    }

    @Override
    public int hashCode() {
        int resultat = url.hashCode();
        resultat = 31 * resultat + methodeHttp.hashCode();
        return resultat;
    }
}
```

`equals()` et `hashCode()` sont obligatoires car `VerbUrl` est utilisee comme cle de HashMap.
Sans surcharge, Java compare les adresses memoire — deux objets au contenu identique
seraient consideres differents.

L'annotation `@UrlMapping` recoit un deuxieme attribut :

```java
public @interface UrlMapping {
    String value();
    String method() default "GET";
}
```

Le HashMap devient :

```java
private HashMap<VerbUrl, Mapping> urlMappingMap = new HashMap<>();
```

Remplissage dans `init()` :

```java
String url         = annotation.value();
String methodeHttp = annotation.method();
VerbUrl cle = new VerbUrl(url, methodeHttp);
urlMappingMap.put(cle, new Mapping(cls.getName(), method.getName()));
```

Recherche dans `processRequest` :

```java
VerbUrl cle = new VerbUrl(requestedUrl, request.getMethod());
if (urlMappingMap.containsKey(cle)) { ... }
```

### Comment — Probleme 2 : execution par reflexion (Prototype)

Le framework ne connait pas `EmpController` — c'est une classe de l'app de test.
On utilise la Reflection API avec uniquement les noms stockes dans `Mapping` :

```java
Class<?> laClasse  = Class.forName(mapping.getClassName());
Object   instance  = laClasse.getDeclaredConstructor().newInstance();
Method   laMethode = laClasse.getDeclaredMethod(mapping.getMethodName());
laMethode.invoke(instance);
```

C'est le pattern Prototype : un nouvel objet est cree a chaque requete, utilise, puis abandonne.

### Exemple

```java
@Controller
public class EmpController {

    @UrlMapping("/employe/liste")
    public void afficherListe() {
        System.out.println("afficherListe executee !");
    }

    @UrlMapping(value = "/employe/liste", method = "POST")
    public void sauvegarder() {
        System.out.println("sauvegarder executee !");
    }
}
```

HashMap :
```
VerbUrl("GET",  "/employe/liste") -> (EmpController, afficherListe)
VerbUrl("POST", "/employe/liste") -> (EmpController, sauvegarder)
```

GET sur `/employe/liste` -> console Tomcat :
```
afficherListe executee !
```

POST sur `/employe/liste` -> console Tomcat :
```
sauvegarder executee !
```

---

## Sprint 5 — ModelAndView, JSP, prefixe/suffixe

### Quoi ?

La methode du controleur doit pouvoir retourner des donnees ET indiquer quelle page JSP
afficher. Le framework se charge de tout le reste.

### Comment ?

Creation de `ModelAndView` :

```java
public class ModelAndView {
    private String url;
    private Map<String, Object> data;

    public ModelAndView() {
        this.data = new HashMap<>();
    }

    public void setUrl(String url)                       { this.url = url; }
    public String getUrl()                               { return url; }
    public void setAttribute(String nom, Object valeur)  { data.put(nom, valeur); }
    public Map<String, Object> getData()                 { return data; }
}
```

Declaration du prefixe et suffixe dans `web.xml` :

```xml
<init-param>
    <param-name>prefixe</param-name>
    <param-value>WEB-INF/views/</param-value>
</init-param>
<init-param>
    <param-name>suffixe</param-name>
    <param-value>.jsp</param-value>
</init-param>
```

`processRequest` traite le `ModelAndView` :

```java
Object resultat = laMethode.invoke(instance);

if (resultat instanceof ModelAndView) {
    ModelAndView mv = (ModelAndView) resultat;

    String cheminJsp = prefixe + mv.getUrl() + suffixe;

    for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
        request.setAttribute(entry.getKey(), entry.getValue());
    }

    request.getRequestDispatcher(cheminJsp).forward(request, response);
}
```

Le controleur retourne un `ModelAndView` :

```java
@UrlMapping("/employe/liste")
public ModelAndView afficherListe() {
    List<String> employes = new ArrayList<>();
    employes.add("Larissa");
    employes.add("Jean");

    ModelAndView mv = new ModelAndView();
    mv.setUrl("employe/liste");
    mv.setAttribute("employes", employes);
    mv.setAttribute("titre", "Liste des Employes");
    return mv;
}
```

La JSP affiche les donnees :

```jsp
<%@ page import="java.util.List" %>
<h2><%= request.getAttribute("titre") %></h2>
<ul>
<%
    List<String> employes = (List<String>) request.getAttribute("employes");
    for (String emp : employes) {
%>
    <li><%= emp %></li>
<% } %>
</ul>
```

### Exemple — flux complet

```
GET localhost:8080/sprint0/employe/liste
        |
        FrontServlet.processRequest()
        |
        trouve VerbUrl("GET", "/employe/liste") dans la map
        |
        instancie EmpController, appelle afficherListe()
        |
        recoit ModelAndView { url="employe/liste", data={employes:[...], titre:"..."} }
        |
        construit "WEB-INF/views/" + "employe/liste" + ".jsp"
        |
        request.setAttribute("employes", [...])
        request.setAttribute("titre", "Liste des Employes")
        |
        forward vers WEB-INF/views/employe/liste.jsp
        |
        page HTML affichee dans le navigateur
```
