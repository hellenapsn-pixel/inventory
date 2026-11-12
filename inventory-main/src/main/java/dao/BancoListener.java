package dao;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** Inicializa o banco de dados e o catálogo do Inventory. */
@WebListener
public class BancoListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        CriarBanco.criarTabela();
        try {
            CriarBanco.carregarCatalogoOpcional();
        } catch (Exception e) {
            System.err.println("Não foi possível sincronizar o catálogo CSV.");
            e.printStackTrace();
        }
    }
}
