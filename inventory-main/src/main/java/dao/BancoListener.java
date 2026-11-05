package dao;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** Inicializa o banco e inicia a sincronização periódica de lançamentos. */
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
        AtualizarLancamentosSteam.iniciar();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        AtualizarLancamentosSteam.parar();
    }
}
