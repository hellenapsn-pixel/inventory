package dao;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** Inicializa o banco uma única vez quando a aplicação sobe. */
@WebListener
public class BancoListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        CriarBanco.criarTabela();
        try {
            CriarBanco.carregarCatalogoOpcional();
            System.out.println("Catálogo do Inventory sincronizado com o banco.");
        } catch (Exception e) {
            System.err.println("Não foi possível sincronizar o catálogo do Inventory.");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
