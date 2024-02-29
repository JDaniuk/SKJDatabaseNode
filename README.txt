Budowa pojedyńczego Node'a:
-każdy Node ma swoje unikalne, generowane automatycznie przy tworzeniu ID;
-każdy Node ma listę połączonych z nim socket'ów;
-Node jest kompilowany za pomocą komendy: Javac DatabaseNode.java lub pliku Compile.bat
-Node rozpoczyna pracę komendą Java DatabaseNode <parametry> gdzie parametrami są komendy takie jak podane w dokumentacji
-parametry mogą być podane w dowolnej kolejności
-Node posiada również: 
	-licznik otrzymanych odpowiedzi akutalizowany przy zapytaniu
	-flagę connectedToClient sprawdzającą czy jest pierwszym Node'm który otrzymał zapytanie od klienta
	-pola klucz i wartość


Budowa połączenia:
-połączenie rozwiązane jest za pomocą Socketów (protokół tcp)
-połączenie ma dwa pola ID: OrginServerId, i connectedToID, sa to pola przechowujące ID serwera który chciał nawiązać połączenie
	i serwera do którego połączenie miało być nawiązane
-każdy socket ma pole typ, równe 1 albo 2. Socket typu 1 jest socketem stworzonym z tego węzła, socket typu 2 jest socketem łączącym się
-Socket typu 1 ma OrginID takie jak obecny serwer, ConnectedToID jak obcy serwer, Socket typu 2 na odwrót
-po połączeniu Node wysyła do serwera do którego się łączy komunikat "-type:server", dzięki któremu serwer pamięta że jest to połączenie do serwera, i przesyła mu swoje id do zapisania w odpowiednim polu


Budowa sieci i opis komunikacji:
-sieć jest tworzona poprzez łączenie ze sobą Node'ów
-reguła nawiązywania połączeń między węzłami:
	1.Tworzymy pierwszy węzeł, nie połączony z żadnym innym
	2.Tworzymy nowy węzeł, chcąć połączyć go z pierwszym
	3.Węzeł drugi próbuje nawiązać połączenie z węzłem pierwszym. Gdy pierwszy zaakceptuje połączenie, drugi wysyła komunikat "-type:server <IDserwera drugiego"
	4.Serwer pierwszy zapamiętuj że nowopowstałe połączenie prowadzi do innego serwera, oraz nadaje mu w odpowiednich polach swoje, i otrzymane id drugiego serwera;
-reguła komunikacji w wypadku większości poleceń jest taka:
	1. Serwer który otrzymał od klienta polecenie jeśli samemu nie jest wstanie go zrealizwac wysyła do swoich połączonych Node'ów to samo połączenie
		do wykonania wewnętrzengo (z dopiskiem -internal z przodu), wraz z listą id swoich połączonych serwerów + jego własne id
	2. Serwer czeka na liczbę odpowiedzi równą liście wysłanych zapytań
	3.Serwer który otrzymał polecenie wewnętrze jeżeli nie jest w stanie wykonać samemu polecenia wysyła je tak samo jak w poprzednim kroku 1 do swoich
		przyłączonych serwerów, ignorując te które otrzymały już zapytanie(są na liście którą otrzymał wraz z poleceniem)
		
	4.Jeśli serwer otrzymał tyle razy komunikat błędu, ile wysłał komunikatów odsyła komunikat błędu
	5.Jeśli serwer jest w stanie wykonać operację, odsyła OK, lub inną wartość zależnie od wysłanej komendy
	6.Jeśli serwer w dowolnym momencie otrzyma OK, przerywa nasłuchiwanie i odsyła dalej OK
	7.Jeśli Serwer który otrzymał od klienta polecenie otrzyma Tyle błędów ile wysłał zapytań, odsyła klientowi ERROR, i zamyka połączenie
	8.Jeśli serwer który otrzymał od klienta polecenie otrzyma komunikat OK, przerywa nasłuchiwanie i odsyła OK lub inną wartość, zależnie od komendy
-więszość komend występuje w wersji zwykłej i z dopiskiem "-internal", komendy z dopiskiem są to komendy przesyłane między serwerami


Przykład komunikacji:
	Mamy sieć zbudowaną na zasadzie:

		   F
		   |
	A -------- B -----E
	|	   |
	|	   |
	|	   |
	C----------D
	gdzie A,B,C,D są węzłami. Serwery przechowują warości: A - 1:1 B - 2:2 C - 3:3 D - 4:4 E - 5:5 F- 6:6
	1.klient łączy się z Serwerem A, i wysyła polecenie set-value 4:16.
	2.Ponieważ serwer A nie jest w stanie wykonać polecenia, wysyła do B i C  komunikat -internal set-value 4:16 <lista ignorowanych serwerów>, gdzie lista ignorowanych serwerów zawiera <IDserweraA,IDserweraB,IDserweraC>
	3.Ponieważ serwer  B nie jest w stanie wykonać polecenia, przesła je do serwerów D,E,F
	4.Ponieważ serwer C nie jest w stanie wykonac polecenia, przesyła je od serewera D
	5. Ponieważ F i E nie są w stanie wykonać polecenia, wysłają polecenie do wszystkich serwerów nie na liście ignorowanych. Serwer B znajduje się na liście, więc wysyłają 0 połączeń. 
		Ponieważ liczba oczekiwanych odpowidzi jest równa 0, odsyłają do serwera B komunikat "internal ERROR"
	6.Serwer D zmiena wartość z 4:4 na 4:16, i odsyła do C i B komunikat "-internal OK", przerywając ich nasłuchiwanie na odpowiedzi
	7. B i C odsyłają -internal OK do serwera A. Serwer A po otrzymaniu odpowiedzi ponieważ jest połączony z klientem odsyła mu komunikat "OK", i zamyka z nim połączenie
 


-Co zostało zaimplementowane:
	-łączenie i rozłączanie węzłów
	-automatyczne nadawanie portu w wypadku zajętego portu ( port zwiększa się o 1, dopóki nie znajdzie wolnego)
	-wszystkie operacje podane w dokumentacji.


-Co nie działa:
	-Jeśli dwóch klientów wyśle zapytanie w tej samej chwili, wystąpią błędy.







	