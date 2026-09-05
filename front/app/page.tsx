import { Box } from "@mui/material";
import { Navbar, Hero, Features, PricingSection, WhatsAppBotSection, ContactForm, WhatsAppButton, Footer } from "@/components/landing";

/**
 * Landing Page principal de Dio Burger
 * Incluye Navbar, Hero, Features, Pricing, Bot de WhatsApp, Formulario de Contacto y Footer
 */
export default function Home() {
  return (
    <Box>
      <Navbar />
      <Hero />
      <Features />
      <PricingSection />
      <WhatsAppBotSection />
      <ContactForm />
      <Footer />
      <WhatsAppButton />
    </Box>
  );
}
