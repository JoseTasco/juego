type Traducciones = { [key: string]: string };

const es: Traducciones = {
  'landing.subtitulo':   'Un reino en llamas',
  'landing.descripcion': 'La corona ha caído en cenizas. Los reinos se fracturan.\nSolo un comandante audaz puede forjar un nuevo orden\ndesde las brasas del conflicto.',
  'landing.pregunta':    '¿Cómo deseas continuar?',
  'landing.crear':       '⚔  Crear cuenta',
  'landing.iniciar':     'Iniciar sesión',
  'landing.nota':        'Al crear una cuenta aceptas los términos del reino',
  'register.titulo':     'Crear cuenta',
  'register.correo':     'Correo electrónico',
  'register.usuario':    'Nombre de usuario',
  'register.password':   'Contraseña',
  'register.boton':      'Crear cuenta',
  'register.ya_cuenta':  '¿Ya tienes cuenta?',
  'register.ir_login':   'Iniciar sesión',
  'register.cargando':   'Creando cuenta...',
  'login.titulo':        'Iniciar sesión',
  'login.usuario':       'Nombre de usuario',
  'login.password':      'Contraseña',
  'login.boton':         'Entrar al reino',
  'login.sin_cuenta':    '¿No tienes cuenta?',
  'login.ir_registro':   'Crear cuenta',
  'login.cargando':      'Verificando...',
  'menu.bienvenida':     'Bienvenido',
  'menu.empezar':        'Empezar juego',
  'menu.continuar':      'Continuar partida',
  'menu.continuar.bloq': 'Sin partida guardada',
  'menu.idioma':         'Idioma',
  'menu.cerrar':         'Cerrar sesión',
  'idioma.titulo':       'Selecciona el idioma',
  'idioma.es':           'Español',
  'idioma.en':           'English',
  'idioma.guardar':      'Guardar',
  'idioma.guardando':    'Guardando...',
  'error.campos':        'Por favor completa todos los campos.',
  'error.red':           'No se pudo conectar con el servidor.',
};

const en: Traducciones = {
  'landing.subtitulo':   'A kingdom in flames',
  'landing.descripcion': 'The crown has fallen to ashes. Kingdoms fracture.\nOnly a bold commander can forge a new order\nfrom the embers of conflict.',
  'landing.pregunta':    'How do you wish to continue?',
  'landing.crear':       '⚔  Create account',
  'landing.iniciar':     'Sign in',
  'landing.nota':        'By creating an account you accept the terms of the realm',
  'register.titulo':     'Create account',
  'register.correo':     'Email address',
  'register.usuario':    'Username',
  'register.password':   'Password',
  'register.boton':      'Create account',
  'register.ya_cuenta':  'Already have an account?',
  'register.ir_login':   'Sign in',
  'register.cargando':   'Creating account...',
  'login.titulo':        'Sign in',
  'login.usuario':       'Username',
  'login.password':      'Password',
  'login.boton':         'Enter the realm',
  'login.sin_cuenta':    "Don't have an account?",
  'login.ir_registro':   'Create account',
  'login.cargando':      'Verifying...',
  'menu.bienvenida':     'Welcome',
  'menu.empezar':        'New game',
  'menu.continuar':      'Continue',
  'menu.continuar.bloq': 'No saved game',
  'menu.idioma':         'Language',
  'menu.cerrar':         'Sign out',
  'idioma.titulo':       'Select language',
  'idioma.es':           'Español',
  'idioma.en':           'English',
  'idioma.guardar':      'Save',
  'idioma.guardando':    'Saving...',
  'error.campos':        'Please fill in all fields.',
  'error.red':           'Could not connect to the server.',
};

const traducciones: { [lang: string]: Traducciones } = { es, en };
let idiomaActual: string = localStorage.getItem('idioma') || 'es';

export function t(clave: string): string {
  return traducciones[idiomaActual]?.[clave] ?? traducciones['es']?.[clave] ?? clave;
}

export function setIdioma(lang: string): void {
  idiomaActual = lang;
  localStorage.setItem('idioma', lang);
}

export function getIdioma(): string { return idiomaActual; }
