<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html class="dark">
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    
    <!-- Tailwind CSS (CDN for matching exactly the React frontend styles) -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
      tailwind.config = {
        darkMode: 'class',
        theme: {
          extend: {
            colors: {
              border: "hsl(var(--border))",
              input: "hsl(var(--input))",
              ring: "hsl(var(--ring))",
              background: "hsl(var(--background))",
              foreground: "hsl(var(--foreground))",
              primary: { DEFAULT: "hsl(var(--primary))", foreground: "hsl(var(--primary-foreground))" },
              secondary: { DEFAULT: "hsl(var(--secondary))", foreground: "hsl(var(--secondary-foreground))" },
              destructive: { DEFAULT: "hsl(var(--destructive))", foreground: "hsl(var(--destructive-foreground))" },
              muted: { DEFAULT: "hsl(var(--muted))", foreground: "hsl(var(--muted-foreground))" },
              accent: { DEFAULT: "hsl(var(--accent))", foreground: "hsl(var(--accent-foreground))" },
              card: { DEFAULT: "hsl(var(--card))", foreground: "hsl(var(--card-foreground))" },
            }
          }
        }
      }
    </script>
    
    <!-- Mimic React CSS Variables -->
    <style>
      :root {
        --background: 0 0% 100%;
        --foreground: 222.2 84% 4.9%;
        --card: 0 0% 100%;
        --card-foreground: 222.2 84% 4.9%;
        --primary: 326 100% 45%;
        --primary-foreground: 210 40% 98%;
        --muted: 210 40% 96.1%;
        --muted-foreground: 215.4 16.3% 46.9%;
        --border: 214.3 31.8% 91.4%;
        --input: 214.3 31.8% 91.4%;
        --ring: 326 100% 45%;
        --radius: 0.5rem;
      }
      .dark {
        --background: 222.2 84% 4.9%;
        --foreground: 210 40% 98%;
        --card: 222.2 84% 4.9%;
        --card-foreground: 210 40% 98%;
        --primary: 326 100% 55%;
        --primary-foreground: 210 40% 98%;
        --muted: 217.2 32.6% 17.5%;
        --muted-foreground: 215 20.2% 65.1%;
        --border: 217.2 32.6% 17.5%;
        --input: 217.2 32.6% 17.5%;
        --ring: 326 100% 55%;
      }
      body {
        margin: 0;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      }
      /* Animated Abstract Background */
      .abstract-bg {
        position: fixed;
        inset: 0;
        z-index: 0;
        overflow: hidden;
        background-color: hsl(var(--background));
      }
      .blob1, .blob2 {
        position: absolute;
        border-radius: 50%;
        filter: blur(80px);
        opacity: 0.3;
      }
      .blob1 {
        top: -10%; left: -10%; width: 50vw; height: 50vw;
        background: hsl(var(--primary));
        animation: float 20s infinite alternate;
      }
      .blob2 {
        bottom: -10%; right: -10%; width: 40vw; height: 40vw;
        background: hsl(300, 100%, 40%);
        animation: float 15s infinite alternate reverse;
      }
      @keyframes float {
        0% { transform: translate(0, 0) scale(1); }
        100% { transform: translate(10%, 10%) scale(1.1); }
      }
      .glass-card {
        background: rgba(255, 255, 255, 0.05);
        backdrop-filter: blur(12px);
        -webkit-backdrop-filter: blur(12px);
        border: 1px solid rgba(255, 255, 255, 0.1);
      }
      .dark .glass-card {
        background: rgba(0, 0, 0, 0.4);
        border: 1px solid rgba(255, 255, 255, 0.05);
      }
    </style>
</head>
<body class="${bodyClass} bg-background text-foreground min-h-screen relative flex items-center justify-center">

    <div class="abstract-bg">
      <div class="blob1"></div>
      <div class="blob2"></div>
    </div>

    <div class="relative z-10 w-full max-w-md p-4">
        <!-- Theme Toggle (Simplified) -->
        <div class="absolute -top-16 right-4">
           <button onclick="document.documentElement.classList.toggle('dark')" class="bg-card p-2 rounded-full shadow-lg border border-border">
             🌙 / ☀️
           </button>
        </div>

        <div class="glass-card rounded-[var(--radius)] shadow-xl p-8 border border-border bg-card/95">
            <header class="mb-6 text-center">
                <h1 class="text-2xl font-bold tracking-tight text-primary flex items-center justify-center gap-2">
                   🌸 Florinhas do Vouga
                </h1>
                <p class="text-muted-foreground text-sm mt-2">
                    <#nested "header">
                </p>
            </header>

            <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                <div class="mb-4 p-3 rounded-md text-sm ${message.type = 'error'?string('bg-red-500/10 text-red-500 border border-red-500/20','bg-green-500/10 text-green-500 border border-green-500/20')}">
                    ${kcSanitize(message.summary)?no_esc}
                </div>
            </#if>

            <#nested "form">

            <div class="mt-4">
                <#nested "info">
            </div>
        </div>
    </div>

</body>
</html>
</#macro>
