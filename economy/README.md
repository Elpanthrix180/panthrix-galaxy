# 💰 Panthrix's Economy

Mod de economía para **Minecraft Java 1.20.1 + Forge 47**, independiente de Panthrix's Galaxy
(mod ID `panthrixseconomy`, su propio `.jar`). Funciona en un jugador y en servidores
(el `.jar` va en el cliente **y** en el servidor).

```bash
./gradlew -p economy build        # .jar en economy/build/libs/
./gradlew -p economy runClient    # probar en el juego
./gradlew -p economy runServer -Psmoke   # pruebas obligatorias automáticas
```

## Versión 0.1.0: actualización oficial (implementada por etapas)

| # | Etapa | Dónde |
|---|-------|-------|
| 1 | Permisos administrativos | `permission/PEPermissions.java` |
| 2 | Registro de países con nombre oficial | `data/Country.java`, `data/EconomyData.java` |
| 3 | Nombre automático de los bancos | `data/BankNames.java`, `EconomyData.bankName()` |
| 4 | Núcleo del Pueblo y sus permisos | `block/TownCoreBlock.java`, `PEEvents.java` |
| 5 | Gran Plaza y fuente | `plaza/PlazaBuilder.java`, `plaza/PlazaSite.java` |
| 6 | Llegada de jugadores nuevos | `plaza/PlazaManager.java` |
| 7 | Pruebas de guardado, carga y permisos | `test/PESmokeTest.java` (GitHub Actions) |

### 1. Permisos
- Nodo de Forge **`panthrixseconomy.admin`**. Sin mod de permisos lo tienen los operadores de
  nivel ≥ 2 (configurable). Con LuckPerms u otro, se gestiona desde allí.
- Nadie es admin por defecto. En un jugador hace falta tener los trucos activados.
- Todo se comprueba **en el servidor**: comandos, colocar y romper bloques oficiales. Los botones
  del chat solo ejecutan comandos que el servidor vuelve a validar, así que modificar el cliente no sirve.
- Los bloques oficiales no tienen receta y solo salen en la pestaña «Utilidades de operador».
  Si alguien que no es admin los coloca (o lo intenta un dispensador), la colocación se deshace.

### 2–3. Países y bancos
- El nombre del banco **no se guarda**: se calcula cada vez a partir del país en el que está la sucursal.
  - `Panthria` → *Banco Nacional de Panthria*, *… — Sucursal 2*, *… — Sucursal 3*
  - `Reino de Aurelia` → *Banco Nacional del Reino de Aurelia*
  - `República de Sol` → *Banco Nacional de la República de Sol*
  - Fuera de cualquier país → *Banco Independiente*
  - El artículo se elige solo; si no acierta: `/pe country article <país> de|del|de la|de los|de las|auto`.
- El país se decide por ubicación: la sucursal pertenece al país de la ciudad o pueblo que la contiene
  (radio configurable). Un admin también puede asignarla a mano (`/pe bank assign`).
- Las cuentas van por **id del país**, no por nombre: renombrar un país no toca saldos, cuentas,
  transferencias ni historial. Todas las sucursales de un país comparten cuentas.
- Si se borra un país, sus cuentas pasan al Banco Independiente (no se pierde dinero).
- Moneda física: **Moneda Panthrix (₱)**. `/banco depositar|retirar|transferir|historial` junto a un mostrador.
  Los países pueden cobrar un impuesto por transferencia que va a su tesorería.

### 4. Núcleo del Pueblo
Bloque de piedra con banda de madera, esquinas de metal remachadas, escudo azul con la «P» dorada
de Panthrix y placa dorada. Al colocarlo un admin, se enlaza con el pueblo de la zona o se crea uno
nuevo. Clic derecho:
- **Todos**: nombre, tipo, país, responsable, bancos y comercios del pueblo.
- **Responsable**: además la tesorería y la economía nacional.
- **Admin**: además botones para renombrar, asignar país, responsable, radio, tesorería, tipo y eliminar.

Para romperlo (o un banco) un admin tiene que hacerlo agachado; antes se le muestran las consecuencias.
Borrar un país, pueblo o comercio con `/pe … delete` también avisa y pide `confirm`.

### 5–6. La Gran Plaza
Plaza de 41×41 con bloques vanilla: fuente de tres niveles con cascadas, cuatro caminos de piedra,
bancos, farolas, cuatro jardines con árbol y flores, setos, Ayuntamiento con el Núcleo (norte),
Banco (este), Mercado con toldo (oeste), tablón de anuncios y cartel de direcciones (sur).
- Se construye **una vez** al crear un mundo nuevo. Busca alrededor del spawn un sitio llano, seco,
  sin cuevas, sin bloques no naturales, sin cofres y sin estructuras (de vanilla o de otros mods).
  Si no lo encuentra, no toca nada y lo avisa en el log.
- En mundos que ya existían no se construye (salvo en la configuración o con `/pe plaza generate`).
- Cada jugador nuevo aparece en la plaza **la primera vez** que entra. Nunca después de morir o
  reconectar, y no se mueve a quien ya llevaba jugando más de 2 minutos en ese mundo.
- El spawn del mundo no se cambia (opcional en la configuración).

## Versión 0.2.0: compra y venta en comercios oficiales
- Un admin añade ofertas con el objeto **en la mano** (la cantidad también cuenta):
  `/pe shop offer <comercio> sell <precio>` (el comercio lo vende) · `buy <precio>` (lo compra) ·
  `remove <n>` · `/pe shop offer <comercio>` muestra la lista con botones.
- Los jugadores, a menos de 8 bloques del comercio: `/tienda` (lista con botones [Comprar]/[Vender]),
  `/tienda comprar <n>`, `/tienda vender <n>`.
- Se paga con la cuenta del banco del país donde está el comercio. Lo que se compra va a la tesorería
  del pueblo; lo que se vende lo paga esa tesorería (si no tiene fondos, no compra). Un comercio fuera
  de pueblos paga sin límite.
- El Mercado de la Gran Plaza empieza vendiendo pan y antorchas y comprando trigo y piedra.
- El `.jar` de Economy se añade también a las Releases de GitHub.

## Versión 0.3.0: empleos y salarios
- Un admin crea empleos en un pueblo: `/pe settlement job <pueblo> add <salario> <plazas> <nombre>`
  (p. ej. `add 20 3 Panadero`) · `remove <nombre>`. También desde los botones del Núcleo del Pueblo.
- Los jugadores, dentro del pueblo: `/trabajo` (lista con botones), `/trabajo unirse <n>`, `/trabajo dejar`.
- Cada día de Minecraft, los empleados **conectados** cobran su salario desde la tesorería del pueblo
  en su cuenta del banco del país. Si la tesorería no alcanza, ese día no cobran (se les avisa).
- Borrar un empleo o un pueblo deja libres a sus trabajadores.

## Versión 0.4.0: pantallas del banco y de la tienda
- **Banco:** clic derecho en un Mostrador de Banco (o `/banco`) abre una pantalla con el saldo, las monedas
  que llevas, campos para depositar, retirar y transferir, y los últimos movimientos.
- **Tienda:** `/tienda` cerca de un comercio abre una pantalla con cada oferta (icono del objeto, cantidad,
  precio) y su botón Comprar o Vender; con más de 7 ofertas hay páginas.
- Las pantallas solo **piden** acciones: el servidor comprueba distancia, saldo y objetos en cada clic.
- El menú en el chat sigue disponible: `/banco chat` y `/tienda lista`. Los administradores también ven
  sus botones de administración en el chat.

## Comandos
**Jugadores:** `/banco` (saldo, depositar, retirar, transferir, historial) · `/tienda` · `/trabajo` · `/economia aqui|paises`

**Admins:** `/pe` abre el menú con botones. Además:
`/pe country create|rename|article|tax|treasury|info|delete|list` ·
`/pe city create <nombre>` · `/pe town create <nombre>` (en tu posición) ·
`/pe settlement info|rename|country|move|radius|type|manager|treasury|delete|list` ·
`/pe shop create|manager|offer|delete|list` · `/pe bank list|assign|auto` ·
`/pe money give <jugador> <cantidad>` · `/pe plaza info|tp|generate`

Configuración: `config/panthrixseconomy-common.toml`.

## Pruebas obligatorias
`test/PESmokeTest.java` arranca un servidor Forge real recién creado (en GitHub, en cada subida) y comprueba:
- dos países generan nombres de banco distintos (también sucursales, artículos y «Banco Independiente»)
- renombrar un país cambia el nombre de sus bancos sin tocar saldos ni historial
- con el despachador de comandos real: un jugador sin permisos no puede crear países, ciudades, pueblos
  ni comercios, ni renombrar países; un administrador sí; borrar exige confirmación
- guardar y cargar conserva todo (bancos, cuentas, pueblos, núcleo, plaza, jugadores recibidos)
- el Núcleo del Pueblo guarda su pueblo en el bloque
- el mundo nuevo tiene una sola Gran Plaza, con agua en la fuente, Núcleo enlazado y banco; repetir la
  generación o reiniciar no crea otra; el comprobador de sitio rechaza construir encima de algo existente
- jugadores nuevos → plaza; reconexión y jugadores que ya empezaron → no se mueven

Probar con dos clientes reales no se puede automatizar aquí: comprueba a mano en un servidor que un jugador
sin OP no ve `/pe`, no puede colocar el Núcleo y sí puede usar `/banco`.

## Sobre MCreator
Este mod está escrito en Java con Forge, no en MCreator. En MCreator se podría hacer casi todo, pero:
- los **permisos de Forge** (nodo `panthrixseconomy.admin`) requieren código personalizado; la alternativa
  sencilla sería comprobar "el jugador es operador" en cada procedimiento;
- el **nombre generado del banco** necesitaría variables globales del mundo (mapa país → nombre) en vez de `SavedData`;
- la **plaza** se haría con una estructura `.nbt` colocada por un procedimiento una sola vez (guardando un
  booleano global), en lugar del constructor por código que comprueba el terreno.
