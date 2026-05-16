<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "form">
    <div id="kc-form">
      <div id="kc-form-wrapper">
        <#if realm.password>
            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post" class="space-y-4">
                <div class="space-y-2">
                    <label for="username" class="text-sm font-medium leading-none"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>NIF ou E-mail<#else>${msg("email")}</#if></label>
                    <input tabindex="1" id="username" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="off" />
                </div>

                <div class="space-y-2">
                    <div class="flex items-center justify-between">
                        <label for="password" class="text-sm font-medium leading-none">Palavra-passe</label>
                        <#if realm.resetPasswordAllowed>
                            <a tabindex="5" href="${url.loginResetCredentialsUrl}" class="text-sm text-primary hover:underline hover:text-primary/90">Esqueceu-se da password?</a>
                        </#if>
                    </div>
                    <input tabindex="2" id="password" class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50" name="password" type="password" autocomplete="off" />
                </div>

                <div class="flex items-center space-x-2 pb-2">
                    <#if realm.rememberMe && !usernameHidden??>
                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary" <#if login.rememberMe??>checked</#if>>
                        <label for="rememberMe" class="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">Manter sessão iniciada</label>
                    </#if>
                </div>

                <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                <button tabindex="4" name="login" id="kc-login" type="submit" class="inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-primary text-primary-foreground hover:bg-primary/90 h-10 px-4 py-2 w-full">
                    Entrar
                </button>
            </form>
        </#if>
      </div>
    </div>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div class="text-center text-sm text-muted-foreground mt-4">
                Não tem conta? <a tabindex="6" href="${url.registrationUrl}" class="text-primary hover:text-primary/90 hover:underline font-medium">Aderir agora</a>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>
